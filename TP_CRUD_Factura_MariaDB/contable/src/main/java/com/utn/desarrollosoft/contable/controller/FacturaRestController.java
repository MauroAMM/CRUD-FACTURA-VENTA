package com.utn.desarrollosoft.contable.controller;

import com.utn.desarrollosoft.contable.entidades.dto.FacturaReporteDTO;
import com.utn.desarrollosoft.contable.entidades.dto.FacturaRequestDTO;
import com.utn.desarrollosoft.contable.servicios.FacturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class FacturaRestController {

    private final FacturaService facturaService;

    public FacturaRestController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    // ---- endpoints de prueba (se mantienen del proyecto original)

    @GetMapping("prueba")
    public String prueba() {
        return "PRUEBA SPRING BOOT";
    }

    @GetMapping("prueba2")
    public String prueba2() {
        return "PRUEBA 2 SPRING BOOT";
    }

    // ---- GET: listado con filtros opcionales y consulta por ID

    @GetMapping("api/facturas")
    public ResponseEntity<List<FacturaReporteDTO>> getFacturas(
            @RequestParam(required = false) Long numeroFactura,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String clienteDenominacion,
            @RequestParam(required = false) String condicionIva,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaHasta) {

        return ResponseEntity.ok(facturaService.listar(
                numeroFactura, clienteId, clienteDenominacion, condicionIva, estado, fechaDesde, fechaHasta));
    }

    @GetMapping("api/facturas/{id}")
    public ResponseEntity<FacturaReporteDTO> getFacturaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.obtenerPorId(id));
    }

    // ---- POST: alta (201 Created + factura generada)

    @PostMapping("api/facturas")
    public ResponseEntity<FacturaReporteDTO> crearFactura(@RequestBody FacturaRequestDTO request) {
        FacturaReporteDTO creada = facturaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // ---- PUT: modificación de cabecera / ítems / estado (200 OK)

    @PutMapping("api/facturas/{id}")
    public ResponseEntity<FacturaReporteDTO> actualizarFactura(
            @PathVariable Long id,
            @RequestBody FacturaRequestDTO request) {
        return ResponseEntity.ok(facturaService.actualizar(id, request));
    }

    // ---- DELETE: eliminación física (204 No Content)

    @DeleteMapping("api/facturas/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        facturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
