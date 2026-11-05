package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Tabla: pais */
@Entity
@Table(name = "pais")
@Getter
@Setter
public class Pais extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pais")
    private Long idPais;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "codigo_iso", nullable = false, length = 2)
    private String codigoIso;

    @Column(name = "nombre_pais", nullable = false, length = 100)
    private String nombrePais;
}
