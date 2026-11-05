package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: clasificacion_edad */
@Entity
@Table(name = "clasificacion_edad")
@Getter
@Setter
public class ClasificacionEdad extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clasificacion")
    private Long idClasificacion;

    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "nombre_clasificacion", nullable = false, length = 100)
    private String nombreClasificacion;

    @Column(name = "edad_minima", nullable = false)
    private Short edadMinima;
}
