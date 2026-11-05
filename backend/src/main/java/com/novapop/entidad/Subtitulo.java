package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: subtitulo */
@Entity
@Table(name = "subtitulo")
@Getter
@Setter
public class Subtitulo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subtitulo")
    private Long idSubtitulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_video", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idioma", nullable = false)
    private Idioma idioma;

    @Column(name = "tipo_subtitulo", nullable = false, length = 20)
    private String tipoSubtitulo;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;
}
