package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class ListaPrecio extends AuditoriaApp {
    private String codigo;
    private String denominacion;
}
