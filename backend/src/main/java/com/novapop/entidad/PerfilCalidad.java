package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: perfil_calidad */
@Entity
@Table(name = "perfil_calidad")
@Getter
@Setter
public class PerfilCalidad extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil_calidad")
    private Long idPerfilCalidad;

    @Column(name = "nombre_calidad", nullable = false, length = 30)
    private String nombreCalidad;

    @Column(name = "resolucion", nullable = false, length = 20)
    private String resolucion;

    @Column(name = "bitrate_kbps", nullable = false)
    private Integer bitrateKbps;
}
