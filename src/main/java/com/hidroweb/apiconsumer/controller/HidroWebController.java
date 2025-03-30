package com.hidroweb.apiconsumer.controller;

import com.hidroweb.apiconsumer.service.HidroWebService;
import org.springframework.web.bind.annotation.*;

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
        return hidroWebService.autenticarUsuario();
    }
}
