package com.hidroweb.apiconsumer.service;

import com.hidroweb.apiconsumer.client.HidroWebClient;
import com.hidroweb.apiconsumer.config.HidroWebConfig;
import com.hidroweb.apiconsumer.entity.Estacao;
import com.hidroweb.apiconsumer.repository.EstacaoRepository;
import com.hidroweb.apiconsumer.utils.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HidroWebService {

    private final HidroWebClient hidroWebClient;
    private final HidroWebConfig hidroWebConfig;
    private final TokenManager tokenManager;

    public HidroWebService(HidroWebClient hidroWebClient, HidroWebConfig hidroWebConfig, TokenManager tokenManager) {
        this.hidroWebClient = hidroWebClient;
        this.hidroWebConfig = hidroWebConfig;
        this.tokenManager = tokenManager;
    }
    @Autowired
    private EstacaoRepository estacaoRepository;
    public Map<String, Object> autenticarUsuario() {
        // Verificar se o token é válido
        Optional<String> tokenOpt = tokenManager.getToken();

        // Se o token não estiver presente ou estiver expirado, realiza a autenticação novamente
        if (tokenOpt.isEmpty()) {
            System.out.println("Token expirado ou inexistente. Realizando nova autenticação...");
            Map<String, Object> resposta = hidroWebClient.autenticar(hidroWebConfig.getIdentificador(), hidroWebConfig.getSenha());

            System.out.println("Resposta da API: " + resposta);

            // Verificar se a resposta foi bem-sucedida e se o token está presente dentro de "items"
            if (resposta != null && resposta.containsKey("items")) {
                Map<String, Object> items = (Map<String, Object>) resposta.get("items");
                if (items.containsKey("tokenautenticacao")) {
                    String novoToken = (String) items.get("tokenautenticacao");
                    tokenManager.setToken(novoToken);
                    System.out.println("Novo token gerado: " + novoToken);
                } else {
                    throw new RuntimeException("Falha na autenticação: token não encontrado na resposta");
                }
            } else {
                throw new RuntimeException("Falha na autenticação: resposta inválida da API");
            }
        } else {
            // Caso o token esteja válido, ele é retornado
            System.out.println("Token válido: " + tokenOpt.get());
        }

        // Retornar o token atual, mesmo que seja o token renovado
        return Map.of("tokenautenticacao", (Object) tokenManager.getToken().get());
    }
    // Método para pegar as estações de todos os estados


    public void getEstacoesParaTodosOsEstados(String authorization) {
        List<String> ufs = List.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        for (String uf : ufs) {
            Map<String, Object> response = hidroWebClient.getEstacoes(authorization, uf);

            if (response.containsKey("items")) {
                List<Map<String, Object>> estacoes = (List<Map<String, Object>>) response.get("items");

                for (Map<String, Object> est : estacoes) {
                    String tipoEstacao = (String) est.get("Tipo_Estacao");

                    // Verifica se o tipo de estação é "Fluviometrica"
                    if ("Fluviometrica".equalsIgnoreCase(tipoEstacao)) {
                        Long codigo = Long.parseLong(est.get("codigoestacao").toString());

                        // Verifica se já existe
                        if (!estacaoRepository.existsById(codigo)) {
                            Estacao estacao = new Estacao();
                            estacao.setCodigoEstacao(codigo);
                            estacao.setEstacaoNome((String) est.get("Estacao_Nome"));
                            estacao.setUfEstacao((String) est.get("UF_Estacao"));
                            estacao.setMunicipioNome((String) est.get("Municipio_Nome"));
                            estacao.setBaciaNome((String) est.get("Bacia_Nome"));
                            estacao.setSubBaciaNome((String) est.get("Sub_Bacia_Nome"));
                            estacao.setRioNome((String) est.get("Rio_Nome"));
                            estacao.setTipoEstacao(tipoEstacao);  // Tipo de estação sempre "Fluviometrica"

                            estacaoRepository.save(estacao);
                        }
                    }
                }
            }
        }
    }
}