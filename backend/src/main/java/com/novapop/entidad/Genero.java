package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: genero */
@Entity
@Table(name = "genero")
@Getter
@Setter
public class Genero extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_genero")
    private Long idGenero;

    @Column(name = "nombre_genero", nullable = false, length = 60)
    private String nombreGenero;
}
