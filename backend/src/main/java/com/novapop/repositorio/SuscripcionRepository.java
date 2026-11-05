package com.novapop.repositorio;

import com.novapop.entidad.SituacionSuscripcion;
import com.novapop.entidad.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    @Query("""
            select s from Suscripcion s join fetch s.plan p join fetch p.perfilCalidadMaxima
             where s.cuenta.idCuenta = :idCuenta and s.situacionSuscripcion = :situacion and s.estadoRegistro = 'A'
            """)
    Optional<Suscripcion> findPorSituacion(@Param("idCuenta") Long idCuenta,
                                           @Param("situacion") SituacionSuscripcion situacion);

    default Optional<Suscripcion> findActiva(Long idCuenta) {
        return findPorSituacion(idCuenta, SituacionSuscripcion.ACTIVA);
    }
}
