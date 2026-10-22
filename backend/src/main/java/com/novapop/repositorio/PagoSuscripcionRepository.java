package com.novapop.repositorio;

import com.novapop.entidad.PagoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PagoSuscripcionRepository extends JpaRepository<PagoSuscripcion, Long> {

    @Query("""
            select p from PagoSuscripcion p join fetch p.suscripcion s join fetch s.plan join fetch p.metodoPago
             where s.cuenta.idCuenta = :idCuenta and p.estadoRegistro = 'A'
             order by p.fechaPago desc
            """)
    List<PagoSuscripcion> findPorCuenta(@Param("idCuenta") Long idCuenta);
}
