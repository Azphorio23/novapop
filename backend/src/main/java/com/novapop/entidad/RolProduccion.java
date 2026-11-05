package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: rol_produccion */
@Entity
@Table(name = "rol_produccion")
@Getter
@Setter
public class RolProduccion extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol_produccion")
    private Long idRolProduccion;

    @Column(name = "nombre_rol_produccion", nullable = false, length = 60)
    private String nombreRolProduccion;
}
