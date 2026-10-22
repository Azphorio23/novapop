package com.novapop.repositorio;

import com.novapop.entidad.Episodio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodioRepository extends JpaRepository<Episodio, Long> {

    /** Episodios de una serie en orden de visionado (temporada, episodio). */
    @Query("""
            select e from Episodio e join fetch e.temporada t left join fetch e.video
             where t.contenido.idContenido = :idContenido
               and e.estadoRegistro = 'A' and t.estadoRegistro = 'A'
             order by t.numeroTemporada, e.numeroEpisodio
            """)
    List<Episodio> findPorSerie(@Param("idContenido") Long idContenido);
}
