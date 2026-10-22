package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class TipoMoneda extends AuditoriaApp {
    private String codigoAfip;
    private String denominacion;
    private String simbolo;
}
