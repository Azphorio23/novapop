package com.novapop.repositorio;

import com.novapop.entidad.CalificacionContenido;
import com.novapop.entidad.CalificacionContenidoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalificacionContenidoRepository extends JpaRepository<CalificacionContenido, CalificacionContenidoId> {

    /** Una fila [promedio, cantidad] con las valoraciones activas del contenido. */
    @Query("""
            select coalesce(avg(c.valoracion), 0), count(c) from CalificacionContenido c
             where c.id.idContenido = :idContenido and c.estadoRegistro = 'A'
            """)
    List<Object[]> resumen(@Param("idContenido") Long idContenido);
}
