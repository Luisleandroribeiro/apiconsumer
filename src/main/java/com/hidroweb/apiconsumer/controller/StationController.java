package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.entity.Station;
import com.hidroweb.apiconsumer.repository.StationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacoes")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }


    @GetMapping("/findAll")
    public ResponseEntity<List<Station>> findAll() {
        List<Station> response = stationRepository.findAll();
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/uf/{uf}")
    public ResponseEntity<List<Station>> listByUf(@PathVariable String uf) {
        if (uf == null || uf.length() != 2) {
            return ResponseEntity.badRequest().build();
        }

        List<Station> response = stationRepository.findByUfEstacao(uf.toUpperCase());
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
}