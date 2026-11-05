package com.novapop.repositorio;

import com.novapop.entidad.PistaAudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PistaAudioRepository extends JpaRepository<PistaAudio, Long> {

    @Query("select p from PistaAudio p join fetch p.idioma where p.video.idVideo = :idVideo and p.estadoRegistro = 'A'")
    List<PistaAudio> findPorVideo(@Param("idVideo") Long idVideo);
}
