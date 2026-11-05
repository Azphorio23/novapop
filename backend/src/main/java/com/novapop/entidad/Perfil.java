package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Tabla: perfil */
@Entity
@Table(name = "perfil")
@Getter
@Setter
public class Perfil extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long idPerfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private Cuenta cuenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idioma_preferido", nullable = false)
    private Idioma idiomaPreferido;

    @Column(name = "nombre_perfil", nullable = false, length = 50)
    private String nombrePerfil;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "es_infantil", nullable = false)
    private Boolean esInfantil;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "pin_control_parental", length = 4)
    private String pinControlParental;
}
