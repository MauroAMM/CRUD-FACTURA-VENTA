package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class CondicionIva extends AuditoriaApp {
    private int codigoAfip;
    private String denominacion;
}
