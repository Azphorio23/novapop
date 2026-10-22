package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: rol */
@Entity
@Table(name = "rol")
@Getter
@Setter
public class Rol extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol;

    @Column(name = "descripcion", length = 255)
    private String descripcion;
}
