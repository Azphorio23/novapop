package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: idioma */
@Entity
@Table(name = "idioma")
@Getter
@Setter
public class Idioma extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_idioma")
    private Long idIdioma;

    @Column(name = "codigo_iso", nullable = false, length = 5)
    private String codigoIso;

    @Column(name = "nombre_idioma", nullable = false, length = 100)
    private String nombreIdioma;
}
