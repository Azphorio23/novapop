package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Tabla: calificacion_contenido */
@Entity
@Table(name = "calificacion_contenido")
@Getter
@Setter
public class CalificacionContenido extends EntidadAuditable {

    @EmbeddedId
    private CalificacionContenidoId id = new CalificacionContenidoId();

    @MapsId("idPerfil")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil")
    private Perfil perfil;

    @MapsId("idContenido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @Column(name = "valoracion", nullable = false)
    private Short valoracion;

    @Column(name = "fecha_calificacion", nullable = false)
    private OffsetDateTime fechaCalificacion;
}
