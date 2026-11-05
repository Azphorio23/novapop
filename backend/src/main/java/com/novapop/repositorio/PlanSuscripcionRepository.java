package com.novapop.repositorio;

import com.novapop.entidad.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {

    @Query("select p from PlanSuscripcion p join fetch p.perfilCalidadMaxima where p.estadoRegistro = 'A' order by p.precioMensual")
    List<PlanSuscripcion> findActivos();
}
