package com.novapop.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Clave primaria compuesta de {@link VideoCalidad}. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCalidadId implements Serializable {

    @Column(name = "id_video")
    private Long idVideo;

    @Column(name = "id_perfil_calidad")
    private Long idPerfilCalidad;
}
