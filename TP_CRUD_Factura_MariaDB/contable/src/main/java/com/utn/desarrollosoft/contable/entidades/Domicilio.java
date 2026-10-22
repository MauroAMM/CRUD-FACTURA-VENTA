package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class Domicilio extends EntityId {
    private String nombreCalle;
    private String numeroCalle;
}
