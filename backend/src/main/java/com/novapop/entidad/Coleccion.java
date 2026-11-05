package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: coleccion */
@Entity
@Table(name = "coleccion")
@Getter
@Setter
public class Coleccion extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coleccion")
    private Long idColeccion;

    @Column(name = "nombre_coleccion", nullable = false, length = 150)
    private String nombreColeccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_coleccion", nullable = false, length = 20)
    private TipoColeccion tipoColeccion;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "orden_vitrina", nullable = false)
    private Short ordenVitrina;
}
