package com.utn.desarrollosoft.contable.repositorios;

import com.utn.desarrollosoft.contable.entidades.TipoMoneda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoMonedaRepositorio extends JpaRepository<TipoMoneda, Long> {
}
