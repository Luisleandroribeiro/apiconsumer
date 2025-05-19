package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.dto.KeyCurveDTO;
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

    @GetMapping("/authenticate")
    public Map<String, Object> autenticar() {
        return hidroWebService.authenticateUser();
    }

    @GetMapping("/stationsInventory")
    public void inventory() {
        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        hidroWebService.getStationsForAllStates(authorization);
    }
    @GetMapping("/liquidDischargeKeyCurve")
    public KeyCurveDTO liquidDischargeKeyCurve(@RequestParam("codigoEstacao") int codigoEstacao) {
        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        List<Map<String, Object>> resultadosBrutos = hidroWebService.getliquidDischargeKeyCurveForId(authorization, codigoEstacao);

        KeyCurveDTO curvaChave = hidroWebService.calculateKeyCurve(resultadosBrutos);

        // Monta a equação em string e seta no DTO
        String equation = hidroWebService.formatEquation(curvaChave.getA(), curvaChave.getB(), curvaChave.getH0());
        curvaChave.setEquation(equation);

        return curvaChave;
    }



}
