package com.novapop.repositorio;

import com.novapop.entidad.Subtitulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubtituloRepository extends JpaRepository<Subtitulo, Long> {

    @Query("""
            select s from Subtitulo s join fetch s.idioma i
             where s.video.idVideo = :idVideo and s.estadoRegistro = 'A'
             order by i.nombreIdioma
            """)
    List<Subtitulo> findPorVideo(@Param("idVideo") Long idVideo);
}
