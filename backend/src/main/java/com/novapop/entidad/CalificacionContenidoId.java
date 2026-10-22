package com.novapop.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Clave primaria compuesta de {@link CalificacionContenido}. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionContenidoId implements Serializable {

    @Column(name = "id_perfil")
    private Long idPerfil;

    @Column(name = "id_contenido")
    private Long idContenido;
}
