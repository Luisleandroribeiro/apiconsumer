package com.hidroweb.apiconsumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "hidroWebClient", url = "https://www.ana.gov.br/hidrowebservice")
public interface HidroWebClient {

    @GetMapping("/EstacoesTelemetricas/OAUth/v1")
    Map<String, Object> autenticar(@RequestHeader("Identificador") String identificador,
                                   @RequestHeader("Senha") String senha);

    // Exemplo de uma chamada para outra API usando o token
    @GetMapping("/AlgumaOutraApi/v1")
    Map<String, Object> makeApiCallWithToken(@RequestHeader("Authorization") String token);
}
