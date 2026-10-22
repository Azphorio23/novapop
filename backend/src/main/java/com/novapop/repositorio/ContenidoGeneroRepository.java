package com.novapop.repositorio;

import com.novapop.entidad.ContenidoGenero;
import com.novapop.entidad.ContenidoGeneroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContenidoGeneroRepository extends JpaRepository<ContenidoGenero, ContenidoGeneroId> {

    @Query("""
            select cg from ContenidoGenero cg join fetch cg.genero g
             where cg.id.idContenido in :ids and cg.estadoRegistro = 'A'
             order by cg.esPrincipal desc, g.nombreGenero
            """)
    List<ContenidoGenero> findActivosPorContenidos(@Param("ids") Collection<Long> idsContenido);
}
