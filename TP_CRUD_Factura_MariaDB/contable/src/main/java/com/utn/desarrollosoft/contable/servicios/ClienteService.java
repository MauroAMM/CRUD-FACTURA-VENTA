package com.utn.desarrollosoft.contable.servicios;

import com.utn.desarrollosoft.contable.entidades.Cliente;
import com.utn.desarrollosoft.contable.entidades.dto.ClienteDTO;
import com.utn.desarrollosoft.contable.repositorios.ClienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Búsqueda de clientes para el explorador (lupa) del frontend. */
@Service
public class ClienteService {

    private final ClienteRepositorio clienteRepositorio;

    public ClienteService(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    /** Busca por denominación o CUIT/CUIL. Con texto vacío devuelve todos los clientes. */
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscar(String texto) {
        String filtro = (texto == null || texto.trim().isEmpty()) ? null : texto.trim();
        List<ClienteDTO> resultado = new ArrayList<>();
        for (Cliente cliente : clienteRepositorio.buscar(filtro)) {
            resultado.add(new ClienteDTO(cliente.getId(), cliente.getCuitCuil(), cliente.getDenominacion()));
        }
        return resultado;
    }
}
