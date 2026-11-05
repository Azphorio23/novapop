package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Variante de un video en un perfil de calidad (streaming adaptativo). Tabla: video_calidad */
@Entity
@Table(name = "video_calidad")
@Getter
@Setter
public class VideoCalidad extends EntidadAuditable {

    @EmbeddedId
    private VideoCalidadId id = new VideoCalidadId();

    @MapsId("idVideo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_video")
    private Video video;

    @MapsId("idPerfilCalidad")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil_calidad")
    private PerfilCalidad perfilCalidad;

    @Column(name = "url_manifiesto", nullable = false, length = 500)
    private String urlManifiesto;

    @Column(name = "tamano_mb", precision = 10, scale = 2)
    private BigDecimal tamanoMb;
}
