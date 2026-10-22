package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: temporada */
@Entity
@Table(name = "temporada")
@Getter
@Setter
public class Temporada extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_temporada")
    private Long idTemporada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido", nullable = false)
    private Contenido contenido;

    @Column(name = "numero_temporada", nullable = false)
    private Short numeroTemporada;

    @Column(name = "titulo_temporada", length = 200)
    private String tituloTemporada;

    @Column(name = "anio_estreno")
    private Short anioEstreno;

    @Column(name = "url_trailer", length = 500)
    private String urlTrailer;
}
