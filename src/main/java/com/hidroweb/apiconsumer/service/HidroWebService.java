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
    private static final Logger logger = LoggerFactory.getLogger(HidroWebService.class);
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
            logger.info("Token expirado ou inexistente. Realizando nova autenticação...");
            Map<String, Object> resposta = hidroWebClient.autenticar(hidroWebConfig.getIdentificador(), hidroWebConfig.getSenha());

            logger.debug("Resposta da API: {}", resposta);

            if (resposta != null && resposta.containsKey(ITEMS_KEY)) {
                Object itemsObj = resposta.get(ITEMS_KEY);

                if (itemsObj instanceof Map<?, ?> itemsMap) {
                    Object tokenObj = itemsMap.get(AUTHENTICATION_TOKEN);

                    if (tokenObj instanceof String novoToken) {
                        tokenManager.setToken(novoToken);
                        logger.info("Novo token gerado: {}", novoToken);
                        return Map.of(AUTHENTICATION_TOKEN, novoToken);
                    } else {
                        logger.error("Falha na autenticação: token não encontrado na resposta");
                        throw new AuthenticationHidroWebException("Token não encontrado na resposta da API.");
                    }
                } else {
                    logger.error("Falha na autenticação: estrutura de dados inesperada");
                    throw new AuthenticationHidroWebException("Estrutura inesperada: 'items' não é um Map.");
                }
            } else {
                logger.error("Falha na autenticação: resposta inválida da API");
                throw new AuthenticationHidroWebException("Resposta inválida: chave 'items' ausente ou resposta nula.");
            }
        } else {
            String token = tokenOpt.get();
            logger.info("Token válido: {}", token);
            return Map.of(AUTHENTICATION_TOKEN, token);
        }
    }




    public void getStationsForAllStates(String authorization)  {
        List<String> ufs = List.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        // Buscar todos os códigos existentes no banco de uma vez
        Set<Long> codigosExistentes = stationRepository.findAllCodigosEstacao();

        List<Station> novasEstacoes = new ArrayList<>();

        for (String uf : ufs) {
            Map<String, Object> response = hidroWebClient.getEstacoes(authorization, uf);

            if (response.containsKey(ITEMS_KEY)) {
                List<Map<String, Object>> estacoes = (List<Map<String, Object>>) response.get(ITEMS_KEY);

                for (Map<String, Object> est : estacoes) {
                    String tipoEstacao = (String) est.get("Tipo_Estacao");

                    if ("Fluviometrica".equalsIgnoreCase(tipoEstacao)) {
                        Long codigo = Long.parseLong(est.get("codigoestacao").toString());

                        if (!codigosExistentes.contains(codigo)) {
                            Station station = new Station();
                            station.setCodigoEstacao(codigo);
                            station.setEstacaoNome((String) est.get("Estacao_Nome"));
                            station.setUfEstacao((String) est.get("UF_Estacao"));
                            station.setMunicipioNome((String) est.get("Municipio_Nome"));
                            station.setBaciaNome((String) est.get("Bacia_Nome"));
                            station.setSubBaciaNome((String) est.get("Sub_Bacia_Nome"));
                            station.setRioNome((String) est.get("Rio_Nome"));
                            station.setTipoEstacao(tipoEstacao);

                            novasEstacoes.add(station);
                            codigosExistentes.add(codigo); // Adiciona na lista para evitar duplicações dentro da mesma execução
                        }
                    }
                }
            }
        }

        // Salva todas as novas estações de uma vez
        if (!novasEstacoes.isEmpty()) {
            stationRepository.saveAll(novasEstacoes);
        }
    }

    public List<Map<String, Object>> getliquidDischargeKeyCurveForId(String authorization, int codigoEstacao) {
        List<Map<String, Object>> resultados = new ArrayList<>();

        LocalDate dataAtual = LocalDate.now();
        LocalDate dataInicial = LocalDate.of(1970, 1, 1);
        int diasPorRequisicao = 366;

        while (dataInicial.isBefore(dataAtual)) {
            LocalDate dataFinal = dataInicial.plusDays(diasPorRequisicao - 1);
            if (dataFinal.isAfter(dataAtual)) {
                dataFinal = dataAtual;
            }

            Map<String, String> params = new HashMap<>();
            params.put("Código da Estação", String.valueOf(codigoEstacao));
            params.put("Tipo Filtro Data", "DATA_LEITURA");
            params.put("Data Inicial (yyyy-MM-dd)", dataInicial.toString());
            params.put("Data Final (yyyy-MM-dd)", dataFinal.toString());

            try {
                Map<String, Object> resposta = hidroWebClient.getliquidDischargeKeyCurve(authorization, params);
                resultados.add(resposta);
                System.out.println("Sucesso: " + dataInicial + " até " + dataFinal);
            } catch (Exception e) {
                System.err.println("Erro ao buscar dados entre " + dataInicial + " e " + dataFinal + ": " + e.getMessage());
            }

            dataInicial = dataFinal.plusDays(1);
        }

        return resultados;
    }
    public KeyCurveDTO calculateKeyCurve(List<Map<String, Object>> results) {
        List<QuotaFlow> pairs = extractValidQuotaFlows(results);

        if (pairs.isEmpty()) {
            logger.warn("Nenhum par válido (quota, flow) foi encontrado para cálculo.");
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
                logger.warn("hFixed <= 0, pulando par: {}", pair);
            }
        }

        if (logH.isEmpty()) {
            logger.warn("Listas para regressão linear estão vazias após filtragem.");
            return new KeyCurveDTO(0, 0, h0);
        }

        return performLinearRegression(logH, logQ, h0);
    }

    private List<QuotaFlow> extractValidQuotaFlows(List<Map<String, Object>> results) {
        List<QuotaFlow> pairs = new ArrayList<>();

        for (Map<String, Object> response : results) {
            List<Map<String, Object>> readings = (List<Map<String, Object>>) response.get("items");
            if (readings == null) {
                logger.warn("Nenhuma leitura encontrada no response: {}", response);
                continue;
            }

            logger.info("Quantidade de leituras encontradas: {}", readings.size());

            for (Map<String, Object> reading : readings) {
                try {
                    Object quotaObj = reading.get("Cota (cm)");
                    Object flowObj = reading.get("Vazao (m3/s)");

                    if (quotaObj == null || flowObj == null) {
                        logger.warn("Campo 'Cota (cm)' ou 'Vazao (m3/s)' ausente em leitura: {}", reading);
                        continue;
                    }

                    double quota = Double.parseDouble(quotaObj.toString());
                    double flow = Double.parseDouble(flowObj.toString());

                    if (quota > 0 && flow > 0) {
                        pairs.add(new QuotaFlow(quota, flow));
                    } else {
                        logger.warn("Valores inválidos (quota <= 0 ou flow <= 0), ignorando leitura: {}", reading);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Erro ao converter valores numéricos na leitura: {} - erro: {}", reading, e.getMessage());
                } catch (Exception e) {
                    logger.error("Erro inesperado ao processar leitura: {} - erro: {}", reading, e.getMessage());
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
            logger.error("Divisão por zero no cálculo da regressão linear, denominador = 0.");
            return new KeyCurveDTO(0, 0, h0);
        }

        double b = (n * sumXY - sumX * sumY) / denominator;
        double logA = (sumY - b * sumX) / n;
        double a = Math.pow(10, logA);

        logger.info("Resultado da regressão: a = {}, b = {}, h0 = {}", a, b, h0);

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
