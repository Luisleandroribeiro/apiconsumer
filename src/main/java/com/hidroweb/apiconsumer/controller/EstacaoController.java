package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.entity.Estacao;
import com.hidroweb.apiconsumer.repository.EstacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacoes")
public class EstacaoController {

    @Autowired
    private EstacaoRepository estacaoRepository;

    @GetMapping("/listarTodas")
    public List<Estacao> listarTodas() {
        return estacaoRepository.findAll();
    }

    @GetMapping("/uf/{uf}")
    public List<Estacao> listarPorUF(@PathVariable String uf) {
        return estacaoRepository.findByUfEstacao(uf.toUpperCase());
    }

    @GetMapping("/tipo/{tipo}")
    public List<Estacao> listarPorTipo(@PathVariable String tipo) {
        return estacaoRepository.findByTipoEstacaoIgnoreCase(tipo);
    }

    @GetMapping("/filtrar")
    public List<Estacao> listarPorUfETipo(@RequestParam String uf, @RequestParam String tipo) {
        return estacaoRepository.findByUfEstacaoAndTipoEstacao(uf.toUpperCase(), tipo);
    }
}
