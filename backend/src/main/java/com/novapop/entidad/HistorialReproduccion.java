package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Progreso de reproducción por perfil (alimenta "Seguir viendo"). Tabla: historial_reproduccion */
@Entity
@Table(name = "historial_reproduccion")
@Getter
@Setter
public class HistorialReproduccion extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido", nullable = false)
    private Contenido contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_episodio")
    private Episodio episodio;

    @Column(name = "segundo_visto", nullable = false)
    private Integer segundoVisto;

    @Column(name = "completado", nullable = false)
    private Boolean completado;

    @Column(name = "ultima_actualizacion", nullable = false)
    private OffsetDateTime ultimaActualizacion;
}
