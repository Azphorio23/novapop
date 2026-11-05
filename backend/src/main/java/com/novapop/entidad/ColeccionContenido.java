package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: coleccion_contenido */
@Entity
@Table(name = "coleccion_contenido")
@Getter
@Setter
public class ColeccionContenido extends EntidadAuditable {

    @EmbeddedId
    private ColeccionContenidoId id = new ColeccionContenidoId();

    @MapsId("idColeccion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_coleccion")
    private Coleccion coleccion;

    @MapsId("idContenido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @Column(name = "orden_aparicion", nullable = false)
    private Short ordenAparicion;
}
