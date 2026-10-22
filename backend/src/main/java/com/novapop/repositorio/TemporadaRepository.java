package com.novapop.repositorio;

import com.novapop.entidad.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TemporadaRepository extends JpaRepository<Temporada, Long> {

    @Query("""
            select t from Temporada t
             where t.contenido.idContenido in :ids and t.estadoRegistro = 'A'
             order by t.numeroTemporada
            """)
    List<Temporada> findActivasPorContenidos(@Param("ids") Collection<Long> idsContenido);
}
