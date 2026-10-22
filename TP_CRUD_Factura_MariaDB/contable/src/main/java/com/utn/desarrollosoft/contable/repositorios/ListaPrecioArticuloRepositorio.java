package com.utn.desarrollosoft.contable.repositorios;

import com.utn.desarrollosoft.contable.entidades.ListaPrecioArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListaPrecioArticuloRepositorio extends JpaRepository<ListaPrecioArticulo, Long> {

    /** Precio de un artículo EN UNA LISTA determinada (Spring Data arma la consulta por el nombre). */
    Optional<ListaPrecioArticulo> findFirstByListaPrecioIdAndArticuloIdOrderByIdAsc(Long listaPrecioId, Long articuloId);

    /** Todos los artículos de una lista, con su precio en esa lista. */
    @Query("select p from ListaPrecioArticulo p where p.listaPrecio.id = :listaPrecioId order by p.articulo.codigo")
    List<ListaPrecioArticulo> listarPorLista(@Param("listaPrecioId") Long listaPrecioId);
}
