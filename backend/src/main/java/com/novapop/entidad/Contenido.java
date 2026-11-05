package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Supertipo de película y serie. Tabla: contenido */
@Entity
@Table(name = "contenido")
@Getter
@Setter
public class Contenido extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenido")
    private Long idContenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clasificacion", nullable = false)
    private ClasificacionEdad clasificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idioma_original", nullable = false)
    private Idioma idiomaOriginal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 10)
    private TipoContenido tipoContenido;

    @Column(name = "titulo_original", nullable = false, length = 200)
    private String tituloOriginal;

    @Column(name = "titulo_localizado", length = 200)
    private String tituloLocalizado;

    @Column(name = "anio_lanzamiento", nullable = false)
    private Short anioLanzamiento;

    @Column(name = "sinopsis", columnDefinition = "text")
    private String sinopsis;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;
}
