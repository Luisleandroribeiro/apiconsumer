package com.hidroweb.apiconsumer.service;

import com.hidroweb.apiconsumer.client.HidroWebClient;
import com.hidroweb.apiconsumer.config.HidroWebConfig;
import com.hidroweb.apiconsumer.dto.KeyCurveDTO;
import com.hidroweb.apiconsumer.entity.QuotaFlow;
import com.hidroweb.apiconsumer.entity.Station;
import com.hidroweb.apiconsumer.exception.AuthenticationHidroWebException;
import com.hidroweb.apiconsumer.repository.StationRepository;
import com.hidroweb.apiconsumer.utils.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class HidroWebService {

    private final HidroWebClient hidroWebClient;
    private final HidroWebConfig hidroWebConfig;
    private final TokenManager tokenManager;
    private final StationRepository stationRepository;
    private static final Logger log = LoggerFactory.getLogger(HidroWebService.class);

    private static final String ITEMS_KEY = "items";
    private static final String AUTHENTICATION_TOKEN = "tokenautenticacao";

    public HidroWebService(HidroWebClient hidroWebClient,
                           HidroWebConfig hidroWebConfig,
                           TokenManager tokenManager,
                           StationRepository stationRepository) {
        this.hidroWebClient = hidroWebClient;
        this.hidroWebConfig = hidroWebConfig;
        this.tokenManager = tokenManager;
        this.stationRepository = stationRepository;
    }

    public Map<String, Object> authenticateUser() {
        Optional<String> tokenOpt = tokenManager.getToken();

        if (tokenOpt.isEmpty()) {
            log.debug("Token expired or non-existent. Performing new authentication...");
            Map<String, Object> response = hidroWebClient.authenticate(hidroWebConfig.getIdentificador(), hidroWebConfig.getSenha());

            log.debug("API Response: {}", response);

            if (response != null && response.containsKey(ITEMS_KEY)) {
                Object itemsObj = response.get(ITEMS_KEY);

                if (itemsObj instanceof Map<?, ?> itemsMap) {
                    Object tokenObj = itemsMap.get(AUTHENTICATION_TOKEN);

                    if (tokenObj instanceof String newToken) {
                        tokenManager.setToken(newToken);
                        log.debug("New token generated: {}", newToken);
                        return Map.of(AUTHENTICATION_TOKEN, newToken);
                    } else {
                        log.debug("Authentication failed: token not found in response");
                        throw new AuthenticationHidroWebException("Token not found in API response.");
                    }
                } else {
                    log.debug("Authentication failed: Unexpected data structure");
                    throw new AuthenticationHidroWebException("Unexpected structure: 'items' is not a Map.");
                }
            } else {
                log.debug("Authentication failed: Invalid API response");
                throw new AuthenticationHidroWebException("Invalid response: missing key 'items' or null response.");
            }
        } else {
            String token = tokenOpt.get();
            log.debug("Valid token: {}", token);
            return Map.of(AUTHENTICATION_TOKEN, token);
        }
    }




    public void getStationsForAllStates(String authorization)  {
        List<String> ufs = List.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        // Search for all existing codes in the database at once
        Set<Long> existingCodes = stationRepository.findAllCodigosEstacao();

        List<Station> newStations = new ArrayList<>();

        for (String uf : ufs) {
            Map<String, Object> response = hidroWebClient.getStations(authorization, uf);

            if (response.containsKey(ITEMS_KEY)) {
                List<Map<String, Object>> stations = (List<Map<String, Object>>) response.get(ITEMS_KEY);

                for (Map<String, Object> est : stations) {
                    String typeStation = (String) est.get("Tipo_Estacao");

                    if ("Fluviometrica".equalsIgnoreCase(typeStation)) {
                        Long codigo = Long.parseLong(est.get("codigoestacao").toString());

                        if (!existingCodes.contains(codigo)) {
                            Station station = new Station();
                            station.setCodigoEstacao(codigo);
                            station.setEstacaoNome((String) est.get("Estacao_Nome"));
                            station.setUfEstacao((String) est.get("UF_Estacao"));
                            station.setMunicipioNome((String) est.get("Municipio_Nome"));
                            station.setBaciaNome((String) est.get("Bacia_Nome"));
                            station.setSubBaciaNome((String) est.get("Sub_Bacia_Nome"));
                            station.setRioNome((String) est.get("Rio_Nome"));
                            station.setTipoEstacao(typeStation);

                            newStations.add(station);
                            existingCodes.add(codigo); // Add to list to avoid duplications within the same run
                        }
                    }
                }
            }
        }

        // Saves all new stations at once
        if (!newStations.isEmpty()) {
            stationRepository.saveAll(newStations);
        }
    }

    public List<Map<String, Object>> getliquidDischargeKeyCurveForId(String authorization, int codigoEstacao) {
        List<Map<String, Object>> results = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = LocalDate.of(1970, 1, 1);
        int daysPorRequest = 366;

        while (startDate.isBefore(currentDate)) {
            LocalDate currenteDate = startDate.plusDays(daysPorRequest - 1);
            if (currenteDate.isAfter(currentDate)) {
                currenteDate = currentDate;
            }

            Map<String, String> params = new HashMap<>();
            params.put("Código da Estação", String.valueOf(codigoEstacao));
            params.put("Tipo Filtro Data", "DATA_LEITURA");
            params.put("Data Inicial (yyyy-MM-dd)", startDate.toString());
            params.put("Data Final (yyyy-MM-dd)", currenteDate.toString());

            try {
                Map<String, Object> response = hidroWebClient.getliquidDischargeKeyCurve(authorization, params);
                results.add(response);
                log.debug("Success: " + startDate + " to " + currenteDate);
            } catch (Exception e) {
                        log.debug("Error fetching data between " + startDate + " and " + currenteDate + ": " + e.getMessage());
            }

            startDate = currenteDate.plusDays(1);
        }

        return results;
    }
    public KeyCurveDTO calculateKeyCurve(List<Map<String, Object>> results) {
        List<QuotaFlow> pairs = extractValidQuotaFlows(results);

        if (pairs.isEmpty()) {
            log.debug("No valid pair (quota, flow) was found for calculation.");
            return new KeyCurveDTO(0, 0, 0);
        }

        double h0 = 0;
        List<Double> logH = new ArrayList<>();
        List<Double> logQ = new ArrayList<>();

        for (QuotaFlow pair : pairs) {
            double hFixed = pair.getQuota() - h0;
            if (hFixed > 0) {
                logH.add(Math.log10(hFixed));
                logQ.add(Math.log10(pair.getFlow()));
            } else {
                log.debug("hFixed <= 0, jumping pair: {}", pair);
            }
        }

        if (logH.isEmpty()) {
            log.debug("Lists for linear regression are empty after filtering.");
            return new KeyCurveDTO(0, 0, h0);
        }

        return performLinearRegression(logH, logQ, h0);
    }

    private List<QuotaFlow> extractValidQuotaFlows(List<Map<String, Object>> results) {
        List<QuotaFlow> pairs = new ArrayList<>();

        for (Map<String, Object> response : results) {
            List<Map<String, Object>> readings = (List<Map<String, Object>>) response.get(ITEMS_KEY);
            if (readings == null) {
                log.debug("No reading found in response: {}", response);
                continue;
            }

                log.debug("Number of readings found: {}", readings.size());

            for (Map<String, Object> reading : readings) {
                try {
                    Object quotaObj = reading.get("Cota (cm)");
                    Object flowObj = reading.get("Vazao (m3/s)");

                    if (quotaObj == null || flowObj == null) {
                        log.debug("'Quota (cm)' or 'Flow rate (m3/s)' field missing when reading: {}", reading);
                        continue;
                    }

                    double quota = Double.parseDouble(quotaObj.toString());
                    double flow = Double.parseDouble(flowObj.toString());

                    if (quota > 0 && flow > 0) {
                        pairs.add(new QuotaFlow(quota, flow));
                    } else {
                        log.debug("Invalid values (quota <= 0 or flow <= 0), skipping reading: {}", reading);
                    }
                } catch (NumberFormatException e) {
                    log.debug("Error converting numeric values to reading: {} - error: {}", reading, e.getMessage());
                } catch (Exception e) {
                    log.debug("Unexpected error while processing read: {} - erro: {}", reading, e.getMessage());
                }
            }
        }
        return pairs;
    }

    private KeyCurveDTO performLinearRegression(List<Double> logH, List<Double> logQ, double h0) {
        int n = logH.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = logH.get(i);
            double y = logQ.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = (n * sumX2 - sumX * sumX);
        if (denominator == 0) {
            log.debug("Division by zero in linear regression calculation, denominator = 0.");
            return new KeyCurveDTO(0, 0, h0);
        }

        double b = (n * sumXY - sumX * sumY) / denominator;
        double logA = (sumY - b * sumX) / n;
        double a = Math.pow(10, logA);

        log.debug("Regression result: a = {}, b = {}, h0 = {}", a, b, h0);

        return new KeyCurveDTO(a, b, h0);
    }
    public String formatEquation(double a, double b, double h0) {
        String aFormatted = String.format("%.4f", a);
        String bFormatted = String.format("%.3f", b);
        String h0Formatted = String.format("%.1f", h0);

        String equation = "y = " + aFormatted + " x^" + bFormatted;

        if (h0 != 0) {
            equation += " + " + h0Formatted;
        }

        return equation;
    }

}
