package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.mapper.StationMapper;
import com.hidroweb.apiconsumer.dto.StationGetResponse;
import com.hidroweb.apiconsumer.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estacoes")
@Slf4j
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final StationMapper mapper;

    @GetMapping("/findAll")
    public ResponseEntity<List<StationGetResponse>> findAll() {
        var stations = stationService.findAll();
        var stationsGetResponse = mapper.toStationGetResponseList(stations);
        return ResponseEntity.ok(stationsGetResponse);
    }

    @GetMapping("/uf/{uf}")
    public ResponseEntity<List<StationGetResponse>> listByUf(@PathVariable String uf) {
        var stations = stationService.findByUf(uf);
        var stationsGetResponse = mapper.toStationGetResponseList(stations);

        return ResponseEntity.ok(stationsGetResponse);
    }
}