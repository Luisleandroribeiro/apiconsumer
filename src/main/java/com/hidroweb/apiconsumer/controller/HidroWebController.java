package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.dto.KeyCurveDTO;
import com.hidroweb.apiconsumer.service.HidroWebService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> authenticate() {
        try {
            Map<String, Object> response = hidroWebService.authenticateUser();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/stationsInventory")
    public ResponseEntity<Void> inventory() {
        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        hidroWebService.getStationsForAllStates(authorization);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/liquidDischargeKeyCurve")
    public ResponseEntity<KeyCurveDTO> liquidDischargeKeyCurve(
            @RequestParam("codigoEstacao") int codigoEstacao,
            @RequestParam(value = "regressionType", defaultValue = "PowerLaw") String regressionType) {

        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        List<Map<String, Object>> rawResults = hidroWebService.getLiquidDischargeKeyCurveForId(authorization, codigoEstacao);

        if (rawResults == null || rawResults.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        KeyCurveDTO keyCurve = hidroWebService.calculateKeyCurve(rawResults, regressionType);

        String equation = hidroWebService.formatEquation(keyCurve);
        keyCurve.setEquation(equation);

        return ResponseEntity.ok(keyCurve);
    }
}