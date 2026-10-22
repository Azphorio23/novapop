package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Tabla: video */
@Entity
@Table(name = "video")
@Getter
@Setter
public class Video extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_video")
    private Long idVideo;

    @Column(name = "duracion_segundos", nullable = false)
    private Integer duracionSegundos;

    @Column(name = "codec_video", nullable = false, length = 20)
    private String codecVideo;

    @Column(name = "fecha_procesamiento")
    private OffsetDateTime fechaProcesamiento;
}
