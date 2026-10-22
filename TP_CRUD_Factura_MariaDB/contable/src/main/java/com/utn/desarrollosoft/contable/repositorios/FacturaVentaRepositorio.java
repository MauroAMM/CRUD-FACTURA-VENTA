package com.utn.desarrollosoft.contable.repositorios;

import com.utn.desarrollosoft.contable.entidades.FacturaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface FacturaVentaRepositorio extends JpaRepository<FacturaVenta, Long> {

    /** Último número de comprobante emitido (null si todavía no hay facturas). */
    @Query("select max(f.numero) from FacturaVenta f")
    Long obtenerUltimoNumero();

    /**
     * Búsqueda con filtros opcionales: un parámetro en null significa "sin filtro".
     * clienteId: cliente exacto (lo usa el explorador de clientes). Cliente (texto): contiene el texto, sin distinguir mayúsculas. Condición de IVA y estado: igualdad
     * sin distinguir mayúsculas. Fechas: rango inclusivo.
     */
    @Query("select f from FacturaVenta f "
            + "where (:numero is null or f.numero = :numero) "
            + "and (:clienteId is null or f.cliente.id = :clienteId) "
            + "and (:cliente is null or lower(f.cliente.denominacion) like lower(concat('%', :cliente, '%'))) "
            + "and (:condicionIva is null or lower(f.condicionIva.denominacion) = lower(:condicionIva)) "
            + "and (:estado is null or lower(f.estado) = lower(:estado)) "
            + "and (:desde is null or f.fechaEmision >= :desde) "
            + "and (:hasta is null or f.fechaEmision <= :hasta) "
            + "order by f.numero")
    List<FacturaVenta> buscar(@Param("numero") Long numero,
                              @Param("clienteId") Long clienteId,
                              @Param("cliente") String cliente,
                              @Param("condicionIva") String condicionIva,
                              @Param("estado") String estado,
                              @Param("desde") Date desde,
                              @Param("hasta") Date hasta);
}
