package com.utn.desarrollosoft.contable.controller;

import com.utn.desarrollosoft.contable.entidades.dto.ClienteDTO;
import com.utn.desarrollosoft.contable.servicios.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ClienteRestController {

    private final ClienteService clienteService;

    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /** Explorador de clientes: GET /api/clientes?texto=... (sin texto devuelve todos). */
    @GetMapping("api/clientes")
    public ResponseEntity<List<ClienteDTO>> getClientes(@RequestParam(required = false) String texto) {
        return ResponseEntity.ok(clienteService.buscar(texto));
    }
}
