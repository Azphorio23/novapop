package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: pista_audio */
@Entity
@Table(name = "pista_audio")
@Getter
@Setter
public class PistaAudio extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pista_audio")
    private Long idPistaAudio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_video", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idioma", nullable = false)
    private Idioma idioma;

    @Column(name = "tipo_pista", nullable = false, length = 20)
    private String tipoPista;

    @Column(name = "canal_audio", nullable = false, length = 10)
    private String canalAudio;
}
