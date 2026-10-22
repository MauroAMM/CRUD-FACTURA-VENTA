package com.utn.desarrollosoft.contable.entidades;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Data;

import java.util.Date;

/** Campos de auditoría comunes. Las fechas se guardan; los usuarios todavía no (no hay tabla de usuarios). */
@Data
@MappedSuperclass
public abstract class AuditoriaApp extends EntityId {
    protected Date fechaAlta;
    protected Date fechaBaja;
    protected Date fechaModificacion;

    @Transient
    protected Usuario usuarioCarga;
    @Transient
    protected Usuario usuarioBaja;
    @Transient
    protected Usuario usuarioModificacion;
}
