package com.novapop.entidad;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Tabla: plan_suscripcion */
@Entity
@Table(name = "plan_suscripcion")
@Getter
@Setter
public class PlanSuscripcion extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long idPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil_calidad_maxima", nullable = false)
    private PerfilCalidad perfilCalidadMaxima;

    @Column(name = "nombre_plan", nullable = false, length = 50)
    private String nombrePlan;

    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "pantallas_simultaneas", nullable = false)
    private Short pantallasSimultaneas;

    @Column(name = "permite_descarga", nullable = false)
    private Boolean permiteDescarga;
}
