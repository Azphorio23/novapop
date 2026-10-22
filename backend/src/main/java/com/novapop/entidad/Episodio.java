package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: episodio */
@Entity
@Table(name = "episodio")
@Getter
@Setter
public class Episodio extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_episodio")
    private Long idEpisodio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_temporada", nullable = false)
    private Temporada temporada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_video") // NULL = solo tráiler, sin video reproducible
    private Video video;

    @Column(name = "numero_episodio", nullable = false)
    private Short numeroEpisodio;

    @Column(name = "titulo_episodio", nullable = false, length = 200)
    private String tituloEpisodio;

    @Column(name = "duracion_minutos", nullable = false)
    private Short duracionMinutos;
}
