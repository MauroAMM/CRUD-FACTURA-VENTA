package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Articulo extends AuditoriaApp {
    @ManyToOne
    private Rubro rubro;
    private String codigo;
    private String denominacion;
    @ManyToOne
    private Marca marca;
}
