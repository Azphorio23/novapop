package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Tabla: mi_lista */
@Entity
@Table(name = "mi_lista")
@Getter
@Setter
public class MiLista extends EntidadAuditable {

    @EmbeddedId
    private MiListaId id = new MiListaId();

    @MapsId("idPerfil")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil")
    private Perfil perfil;

    @MapsId("idContenido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @Column(name = "fecha_agregado", nullable = false)
    private OffsetDateTime fechaAgregado;
}
