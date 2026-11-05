package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tabla: metodo_pago */
@Entity
@Table(name = "metodo_pago")
@Getter
@Setter
public class MetodoPago extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Long idMetodoPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private Cuenta cuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_metodo", nullable = false, length = 20)
    private TipoMetodoPago tipoMetodo;

    @Column(name = "numero_enmascarado", nullable = false, length = 30)
    private String numeroEnmascarado;

    @Column(name = "es_predeterminado", nullable = false)
    private Boolean esPredeterminado;
}
