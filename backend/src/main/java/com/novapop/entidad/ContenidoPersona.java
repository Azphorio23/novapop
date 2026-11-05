package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: contenido_persona */
@Entity
@Table(name = "contenido_persona")
@Getter
@Setter
public class ContenidoPersona extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenido_persona")
    private Long idContenidoPersona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido", nullable = false)
    private Contenido contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol_produccion", nullable = false)
    private RolProduccion rolProduccion;

    @Column(name = "nombre_personaje", length = 150)
    private String nombrePersonaje;

    @Column(name = "orden_creditos", nullable = false)
    private Short ordenCreditos;
}
