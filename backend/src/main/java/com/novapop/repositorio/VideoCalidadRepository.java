package com.novapop.repositorio;

import com.novapop.entidad.VideoCalidad;
import com.novapop.entidad.VideoCalidadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoCalidadRepository extends JpaRepository<VideoCalidad, VideoCalidadId> {

    /** Variantes del video hasta el bitrate máximo permitido por el plan, de mayor a menor calidad. */
    @Query("""
            select vc from VideoCalidad vc join fetch vc.perfilCalidad pc
             where vc.id.idVideo = :idVideo and vc.estadoRegistro = 'A' and pc.bitrateKbps <= :bitrateMaximo
             order by pc.bitrateKbps desc
            """)
    List<VideoCalidad> findPermitidas(@Param("idVideo") Long idVideo, @Param("bitrateMaximo") int bitrateMaximo);
}
