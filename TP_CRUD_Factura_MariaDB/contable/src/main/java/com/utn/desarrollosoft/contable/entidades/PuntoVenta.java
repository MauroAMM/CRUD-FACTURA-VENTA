package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class PuntoVenta extends AuditoriaApp {
    private int numero;
    private String descripcion;
    private String tipoEmision;
    private String domicilioComercial;
}
