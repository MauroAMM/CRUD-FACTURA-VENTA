package com.utn.desarrollosoft.contable.repositorios;

import com.utn.desarrollosoft.contable.entidades.PuntoVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuntoVentaRepositorio extends JpaRepository<PuntoVenta, Long> {
}
