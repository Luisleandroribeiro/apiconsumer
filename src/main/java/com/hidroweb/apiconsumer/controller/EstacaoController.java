package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.entity.Station;
import com.hidroweb.apiconsumer.repository.StationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacoes")
public class EstacaoController {

    private final StationRepository stationRepository;

    public EstacaoController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }


    @GetMapping("/listarTodas")
    public List<Station> listarTodas() {
        return stationRepository.findAll();
    }

    @GetMapping("/uf/{uf}")
    public List<Station> listarPorUF(@PathVariable String uf) {
        return stationRepository.findByUfEstacao(uf.toUpperCase());
    }


}
