package com.novapop.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Clave primaria compuesta de {@link ContenidoProductora}. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoProductoraId implements Serializable {

    @Column(name = "id_contenido")
    private Long idContenido;

    @Column(name = "id_productora")
    private Long idProductora;
}
