package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class Contacto extends EntityId {
    private String email;
    private String telefono;
    private String celular;
}
