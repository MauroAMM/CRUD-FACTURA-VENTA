package com.utn.desarrollosoft.contable.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClienteController {

    @RequestMapping(value = "nombrecliente")
    public String nombreCliente(){
        return "JUAN PEREZ";
    }

    @GetMapping("nombrecliente2")
    public String prueba2(){
        return "Carlos Sanchez";
    }

}
