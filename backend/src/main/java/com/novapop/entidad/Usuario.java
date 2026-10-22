package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Personal interno de la plataforma. Tabla: usuario */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "nombre_usuario", nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "nombres", nullable = false, length = 150)
    private String nombres;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @Column(name = "clave_hash", length = 100)
    private String claveHash;
}
