package com.novapop.entidad;

import com.novapop.auditoria.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Los 7 campos de auditoría que comparten las 35 tablas.
 * Los rellena automáticamente {@link AuditoriaListener} al insertar y actualizar.
 */
@MappedSuperclass
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
public abstract class EntidadAuditable {

    public static final String ACTIVO = "A";
    public static final String INACTIVO = "I";

    @Column(name = "usuario_creacion", nullable = false, updatable = false, length = 100)
    private String usuarioCreacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "ip_creacion", updatable = false, length = 45)
    private String ipCreacion;

    @Column(name = "usuario_modificacion", length = 100)
    private String usuarioModificacion;

    @Column(name = "fecha_modificacion")
    private OffsetDateTime fechaModificacion;

    @Column(name = "ip_modificacion", length = 45)
    private String ipModificacion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado_registro", nullable = false, length = 1)
    private String estadoRegistro = ACTIVO;

    public boolean estaActivo() {
        return ACTIVO.equals(estadoRegistro);
    }

    /** Borrado lógico: el registro se conserva pero deja de mostrarse. */
    public void inactivar() {
        this.estadoRegistro = INACTIVO;
    }
}
