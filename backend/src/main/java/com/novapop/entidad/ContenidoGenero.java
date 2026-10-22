package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: contenido_genero */
@Entity
@Table(name = "contenido_genero")
@Getter
@Setter
public class ContenidoGenero extends EntidadAuditable {

    @EmbeddedId
    private ContenidoGeneroId id = new ContenidoGeneroId();

    @MapsId("idContenido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @MapsId("idGenero")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_genero")
    private Genero genero;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal;
}
