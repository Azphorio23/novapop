package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Ventana de licencia de un contenido en un país. Tabla: contenido_pais */
@Entity
@Table(name = "contenido_pais")
@Getter
@Setter
public class ContenidoPais extends EntidadAuditable {

    @EmbeddedId
    private ContenidoPaisId id = new ContenidoPaisId();

    @MapsId("idContenido")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenido")
    private Contenido contenido;

    @MapsId("idPais")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pais")
    private Pais pais;

    @Column(name = "fecha_disponible_desde", nullable = false)
    private LocalDate fechaDisponibleDesde;

    @Column(name = "fecha_disponible_hasta")
    private LocalDate fechaDisponibleHasta;
}
