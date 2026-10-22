package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class Rubro extends AuditoriaApp {
    private String denominacion;
    private Integer codigo;
}
