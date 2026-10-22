package com.utn.desarrollosoft.contable.repositorios;

import com.utn.desarrollosoft.contable.entidades.ListaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListaPrecioRepositorio extends JpaRepository<ListaPrecio, Long> {

    Optional<ListaPrecio> findByCodigo(String codigo);

    /** La lista con menor ID: se usa como lista por defecto. */
    Optional<ListaPrecio> findFirstByOrderByIdAsc();
}
