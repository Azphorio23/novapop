package com.novapop.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Clave primaria compuesta de {@link ColeccionContenido}. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColeccionContenidoId implements Serializable {

    @Column(name = "id_coleccion")
    private Long idColeccion;

    @Column(name = "id_contenido")
    private Long idContenido;
}
