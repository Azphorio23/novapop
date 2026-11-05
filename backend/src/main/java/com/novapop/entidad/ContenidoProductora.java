package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: contenido_productora */
@Entity
@Table(name = "contenido_productora")
@Getter
@Setter
public class ContenidoProductora extends EntidadAuditable {

    @EmbeddedId
    private ContenidoProductoraId id = new ContenidoProductoraId();

    @MapsId("idContenido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @MapsId("idProductora")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_productora")
    private Productora productora;

    @Column(name = "es_productora_principal", nullable = false)
    private Boolean esProductoraPrincipal;
}
