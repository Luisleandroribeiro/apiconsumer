package com.hidroweb.apiconsumer.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TokenManager {

    private String tokenAutenticacao;
    private Instant tokenExpirationTime;

    // Tempo de validade do token (por exemplo, 10 minutos)
    private static final long TOKEN_VALIDITY_PERIOD = 600000; // 10 minutos em milissegundos

    // Método para obter o token, retornando vazio se o token estiver expirado
    public Optional<String> getToken() {
        if (tokenAutenticacao != null && !isTokenExpired()) {
            // Se o token não expirou, retorna o token
            return Optional.of(tokenAutenticacao);
        } else {
            // Caso o token tenha expirado ou não esteja disponível, retorna um valor vazio
            return Optional.empty();
        }
    }

    // Método para armazenar o token e definir a sua validade
    public void setToken(String token) {
        this.tokenAutenticacao = token;
        // Define o tempo de expiração do token
        this.tokenExpirationTime = Instant.now().plusMillis(TOKEN_VALIDITY_PERIOD);
    }

    // Método que verifica se o token expirou
    public boolean isTokenExpired() {
        // Verifica se o token já expirou
        return Instant.now().isAfter(tokenExpirationTime);
    }
}
