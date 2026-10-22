package com.utn.desarrollosoft.contable.repositorios;

import com.utn.desarrollosoft.contable.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    /**
     * Explorador de clientes: busca por denominación o por CUIT/CUIL (contiene, sin distinguir
     * mayúsculas). Con texto null devuelve todos. Ordenado por denominación.
     */
    @Query("select c from Cliente c "
            + "where (:texto is null "
            + "or lower(c.denominacion) like lower(concat('%', :texto, '%')) "
            + "or lower(c.cuitCuil) like lower(concat('%', :texto, '%'))) "
            + "order by c.denominacion")
    List<Cliente> buscar(@Param("texto") String texto);
}
