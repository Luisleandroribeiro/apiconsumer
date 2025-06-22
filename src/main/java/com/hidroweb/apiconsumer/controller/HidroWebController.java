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
    public ResponseEntity<KeyCurveDTO> liquidDischargeKeyCurve(@RequestParam("codigoEstacao") int codigoEstacao) {
        Map<String, Object> tokenResponse = hidroWebService.authenticateUser();
        String authorization = "Bearer " + tokenResponse.get("tokenautenticacao");

        List<Map<String, Object>> rawResults = hidroWebService.getliquidDischargeKeyCurveForId(authorization, codigoEstacao);

        if (rawResults == null || rawResults.isEmpty()) {
            return ResponseEntity.noContent().build();  // 204 se não houver dados
        }

        KeyCurveDTO keyCurve = hidroWebService.calculateKeyCurve(rawResults);
        String equation = hidroWebService.formatEquation(keyCurve.getA(), keyCurve.getB(), keyCurve.getH0());
        keyCurve.setEquation(equation);

        return ResponseEntity.ok(keyCurve);
    }
}