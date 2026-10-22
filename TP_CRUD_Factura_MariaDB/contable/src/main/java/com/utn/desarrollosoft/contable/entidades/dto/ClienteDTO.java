package com.utn.desarrollosoft.contable.entidades.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Cliente tal como lo muestra el explorador de clientes (lupa). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String cuitCuil;
    private String denominacion;
}
