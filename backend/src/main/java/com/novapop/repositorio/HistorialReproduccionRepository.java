package com.novapop.repositorio;

import com.novapop.entidad.HistorialReproduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistorialReproduccionRepository extends JpaRepository<HistorialReproduccion, Long> {

    @Query("""
            select h from HistorialReproduccion h left join fetch h.episodio
             where h.perfil.idPerfil = :idPerfil and h.estadoRegistro = 'A'
             order by h.ultimaActualizacion desc
            """)
    List<HistorialReproduccion> findPorPerfil(@Param("idPerfil") Long idPerfil);

    @Query("""
            select h from HistorialReproduccion h
             where h.perfil.idPerfil = :idPerfil and h.contenido.idContenido = :idContenido and h.episodio is null
            """)
    Optional<HistorialReproduccion> findDePelicula(@Param("idPerfil") Long idPerfil, @Param("idContenido") Long idContenido);

    @Query("""
            select h from HistorialReproduccion h
             where h.perfil.idPerfil = :idPerfil and h.episodio.idEpisodio = :idEpisodio
            """)
    Optional<HistorialReproduccion> findDeEpisodio(@Param("idPerfil") Long idPerfil, @Param("idEpisodio") Long idEpisodio);
}
