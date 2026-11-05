package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Tabla: dispositivo */
@Entity
@Table(name = "dispositivo")
@Getter
@Setter
public class Dispositivo extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dispositivo")
    private Long idDispositivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private Cuenta cuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dispositivo", nullable = false, length = 15)
    private TipoDispositivo tipoDispositivo;

    @Column(name = "nombre_dispositivo", nullable = false, length = 100)
    private String nombreDispositivo;

    @Column(name = "ultima_conexion")
    private OffsetDateTime ultimaConexion;
}
