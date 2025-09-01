package com.hidroweb.apiconsumer.service;

import com.hidroweb.apiconsumer.client.HidroWebClient;
import com.hidroweb.apiconsumer.config.HidroWebConfig;
import com.hidroweb.apiconsumer.dto.KeyCurveDTO;
import com.hidroweb.apiconsumer.domain.QuotaFlow;
import com.hidroweb.apiconsumer.domain.Station;
import com.hidroweb.apiconsumer.exception.AuthenticationHidroWebException;
import com.hidroweb.apiconsumer.regression.RegressionFactory;
import com.hidroweb.apiconsumer.regression.RegressionModel;
import com.hidroweb.apiconsumer.regression.RegressionResult;
import com.hidroweb.apiconsumer.repository.StationRepository;
import com.hidroweb.apiconsumer.utils.TokenManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HidroWebService {

    private final HidroWebClient hidroWebClient;
    private final HidroWebConfig hidroWebConfig;
    private final TokenManager tokenManager;
    private final StationRepository stationRepository;
    private static final Logger log = LoggerFactory.getLogger(HidroWebService.class);

    private static final String ITEMS_KEY = "items";
    private static final String AUTHENTICATION_TOKEN = "tokenautenticacao";



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

    public List<Map<String, Object>> getLiquidDischargeKeyCurveForId(String authorization, int codigoEstacao) {
        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = LocalDate.of(1970, 1, 1);
        int daysPerRequest = 366;

        while (startDate.isBefore(currentDate)) {
            LocalDate endDate = startDate.plusDays(daysPerRequest - 1);
            if (endDate.isAfter(currentDate)) {
                endDate = currentDate;
            }

            Map<String, String> params = new HashMap<>();
            params.put("Código da Estação", String.valueOf(codigoEstacao));
            params.put("Tipo Filtro Data", "DATA_LEITURA");
            params.put("Data Inicial (yyyy-MM-dd)", startDate.toString());
            params.put("Data Final (yyyy-MM-dd)", endDate.toString());

            try {
                Map<String, Object> response = hidroWebClient.getliquidDischargeKeyCurve(authorization, params);

                if (response != null && response.containsKey("items")) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    results.addAll(items); // adiciona cada leitura individual
                }

                log.debug("Success: {} to {}", startDate, endDate);
            } catch (Exception e) {
                log.debug("Error fetching data between {} and {}: {}", startDate, endDate, e.getMessage());
            }

            startDate = endDate.plusDays(1);
        }

        return results;
    }

    public List<Double> getQuotas(List<Map<String, Object>> readings) {
        List<Double> quotas = new ArrayList<>();
        for (Map<String, Object> reading : readings) {
            Object quotaObj = reading.get("Cota (cm)");
            if (quotaObj != null) {
                try {
                    double quota = Double.parseDouble(quotaObj.toString());
                    if (quota > 0) quotas.add(quota);
                } catch (NumberFormatException e) {
                    log.debug("Invalid quota value: {}", quotaObj);
                }
            }
        }
        return quotas;
    }

    public List<Double> getFlows(List<Map<String, Object>> readings) {
        List<Double> flows = new ArrayList<>();
        for (Map<String, Object> reading : readings) {
            Object flowObj = reading.get("Vazao (m3/s)");
            if (flowObj != null) {
                try {
                    double flow = Double.parseDouble(flowObj.toString());
                    if (flow > 0) flows.add(flow);
                } catch (NumberFormatException e) {
                    log.debug("Invalid flow value: {}", flowObj);
                }
            }
        }
        return flows;
    }

    /**
     * Calculates the key curve using the selected regression model.
     */
    public KeyCurveDTO calculateKeyCurve(List<Map<String, Object>> readings, String regressionType) {
        List<Double> quotas = getQuotas(readings);
        List<Double> flows = getFlows(readings);

        if (quotas.isEmpty() || flows.isEmpty() || quotas.size() != flows.size()) {
            log.debug("No valid data for regression");
            return new KeyCurveDTO(Collections.emptyList(), 0, "No data", regressionType, 0);
        }

        RegressionModel model = RegressionFactory.getRegressionModel(regressionType);
        RegressionResult result = model.fit(quotas, flows);

        KeyCurveDTO dto = new KeyCurveDTO();
        dto.setCoefficients(result.getCoefficients());
        dto.setH0(0); // se o modelo precisar de h0, ajuste aqui
        dto.setModelName(result.getModelName());
        dto.setRSquared(result.getRSquared());

        // Formata a equação
        String equation = formatEquation(dto);
        dto.setEquation(equation);

        return dto;
    }


    public String formatEquation(KeyCurveDTO keyCurve) {
        List<Double> coefs = keyCurve.getCoefficients();
        String model = keyCurve.getModelName();

        if (coefs.isEmpty()) return "No data";

        StringBuilder sb = new StringBuilder();

        if (model.toLowerCase().contains("polynomial")) {
            sb.append("y = ");
            for (int i = coefs.size() - 1; i >= 0; i--) {
                double coef = coefs.get(i);
                if (coef == 0) continue;

                if (sb.length() > 4) { // já adicionou algum termo
                    sb.append(coef > 0 ? " + " : " - ");
                    coef = Math.abs(coef);
                }

                if (i == 0) sb.append(String.format("%.4f", coef));
                else if (i == 1) sb.append(String.format("%.4f*x", coef));
                else sb.append(String.format("%.4f*x^%d", coef, i));
            }
        } else { // PowerLaw ou outros modelos
            double a = coefs.size() > 0 ? coefs.get(0) : 0;
            double b = coefs.size() > 1 ? coefs.get(1) : 0;
            sb.append(String.format("Q = %.4f * (h - %.4f)^%.4f", a, keyCurve.getH0(), b));
        }

        return sb.toString();
    }


}
