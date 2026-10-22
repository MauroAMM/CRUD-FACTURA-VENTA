package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class Cliente extends AuditoriaApp {
    private String cuitCuil;
    private String denominacion;
    @OneToOne(cascade = CascadeType.ALL)
    private Contacto contacto;
    @OneToOne(cascade = CascadeType.ALL)
    private Domicilio domicilio;
}
