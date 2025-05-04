package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.service.HidroWebService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hidroweb")
public class HidroWebController {

    private final HidroWebService hidroWebService;

    public HidroWebController(HidroWebService hidroWebService) {
        this.hidroWebService = hidroWebService;
    }

    @GetMapping("/autenticar")
    public Map<String, Object> autenticar() {
        return hidroWebService.authenticateUser();
    }

    @GetMapping("/inventarioEstacoes")
    public void inventario() {
        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        hidroWebService.getStationsForAllStates(authorization);
    }
    @GetMapping("/liquidDischargeKeyCurve")
    public List<Map<String, Object>> liquidDischargeKeyCurve(@RequestParam("codigoEstacao") int codigoEstacao) {
        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        return hidroWebService.getliquidDischargeKeyCurveForId(authorization, codigoEstacao);
    }

}
