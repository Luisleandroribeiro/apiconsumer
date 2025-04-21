package com.hidroweb.apiconsumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hidroWebClient", url = "https://www.ana.gov.br/hidrowebservice")
public interface HidroWebClient {

    // Método para autenticação
    @GetMapping("/EstacoesTelemetricas/OAUth/v1")
    Map<String, Object> autenticar(@RequestHeader("Identificador") String identificador,
                                   @RequestHeader("Senha") String senha);

    // Exemplo de chamada para obter as estações para um estado específico
    @GetMapping("/EstacoesTelemetricas/HidroInventarioEstacoes/v1")
    Map<String, Object> getEstacoes(@RequestHeader("Authorization") String authorization,
                                    @RequestParam("Unidade%20Federativa") String uf);
}