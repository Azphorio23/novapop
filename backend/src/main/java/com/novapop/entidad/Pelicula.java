package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Subtipo de CONTENIDO: comparte su clave primaria. Tabla: pelicula */
@Entity
@Table(name = "pelicula")
@Getter
@Setter
public class Pelicula extends EntidadAuditable {

    @Id
    @Column(name = "id_contenido")
    private Long idContenido;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_video") // NULL = solo tráiler, sin video reproducible
    private Video video;

    @Column(name = "duracion_minutos", nullable = false)
    private Short duracionMinutos;

    @Column(name = "url_trailer", length = 500)
    private String urlTrailer;
}
