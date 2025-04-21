package com.hidroweb.apiconsumer.service;

import com.hidroweb.apiconsumer.client.HidroWebClient;
import com.hidroweb.apiconsumer.config.HidroWebConfig;
import com.hidroweb.apiconsumer.entity.Estacao;
import com.hidroweb.apiconsumer.exception.AutenticacaoHidroWebException;
import com.hidroweb.apiconsumer.repository.EstacaoRepository;
import com.hidroweb.apiconsumer.utils.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HidroWebService {

    private final HidroWebClient hidroWebClient;
    private final HidroWebConfig hidroWebConfig;
    private final TokenManager tokenManager;
    private final EstacaoRepository estacaoRepository;
    private static final Logger logger = LoggerFactory.getLogger(HidroWebService.class);
    private static final String ITEMS_KEY = "items";
    private static final String AUTHENTICATION_TOKEN = "tokenautenticacao";

    public HidroWebService(HidroWebClient hidroWebClient,
                           HidroWebConfig hidroWebConfig,
                           TokenManager tokenManager,
                           EstacaoRepository estacaoRepository) {
        this.hidroWebClient = hidroWebClient;
        this.hidroWebConfig = hidroWebConfig;
        this.tokenManager = tokenManager;
        this.estacaoRepository = estacaoRepository;
    }

    public Map<String, Object> autenticarUsuario() {
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
                        throw new AutenticacaoHidroWebException("Token não encontrado na resposta da API.");
                    }
                } else {
                    logger.error("Falha na autenticação: estrutura de dados inesperada");
                    throw new AutenticacaoHidroWebException("Estrutura inesperada: 'items' não é um Map.");
                }
            } else {
                logger.error("Falha na autenticação: resposta inválida da API");
                throw new AutenticacaoHidroWebException("Resposta inválida: chave 'items' ausente ou resposta nula.");
            }
        } else {
            String token = tokenOpt.get();
            logger.info("Token válido: {}", token);
            return Map.of(AUTHENTICATION_TOKEN, token);
        }
    }




    @SuppressWarnings("unchecked")
    public void getEstacoesParaTodosOsEstados(String authorization) {
        List<String> ufs = List.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        for (String uf : ufs) {
            Map<String, Object> response = hidroWebClient.getEstacoes(authorization, uf);

            if (response.containsKey(ITEMS_KEY)) {
                List<Map<String, Object>> estacoes = (List<Map<String, Object>>) response.get(ITEMS_KEY);

                for (Map<String, Object> est : estacoes) {
                    String tipoEstacao = (String) est.get("Tipo_Estacao");

                    if ("Fluviometrica".equalsIgnoreCase(tipoEstacao)) {
                        Long codigo = Long.parseLong(est.get("codigoestacao").toString());

                        if (!estacaoRepository.existsById(codigo)) {
                            Estacao estacao = new Estacao();
                            estacao.setCodigoEstacao(codigo);
                            estacao.setEstacaoNome((String) est.get("Estacao_Nome"));
                            estacao.setUfEstacao((String) est.get("UF_Estacao"));
                            estacao.setMunicipioNome((String) est.get("Municipio_Nome"));
                            estacao.setBaciaNome((String) est.get("Bacia_Nome"));
                            estacao.setSubBaciaNome((String) est.get("Sub_Bacia_Nome"));
                            estacao.setRioNome((String) est.get("Rio_Nome"));
                            estacao.setTipoEstacao(tipoEstacao);

                            estacaoRepository.save(estacao);
                        }
                    }
                }
            }
        }
    }

}
