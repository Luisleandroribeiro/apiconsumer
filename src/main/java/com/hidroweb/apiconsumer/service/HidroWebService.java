package com.hidroweb.apiconsumer.service;

import com.hidroweb.apiconsumer.client.HidroWebClient;
import com.hidroweb.apiconsumer.config.HidroWebConfig;
import com.hidroweb.apiconsumer.utils.TokenManager;
import org.springframework.stereotype.Service;

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

}
