package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: productora */
@Entity
@Table(name = "productora")
@Getter
@Setter
public class Productora extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_productora")
    private Long idProductora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;

    @Column(name = "nombre_productora", nullable = false, length = 150)
    private String nombreProductora;
}
