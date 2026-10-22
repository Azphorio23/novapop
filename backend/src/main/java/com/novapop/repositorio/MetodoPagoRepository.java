package com.novapop.repositorio;

import com.novapop.entidad.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    @Query("""
            select m from MetodoPago m
             where m.cuenta.idCuenta = :idCuenta and m.estadoRegistro = 'A'
             order by m.esPredeterminado desc, m.idMetodoPago
            """)
    List<MetodoPago> findActivos(@Param("idCuenta") Long idCuenta);

    @Query("select m from MetodoPago m where m.cuenta.idCuenta = :idCuenta and m.esPredeterminado = true and m.estadoRegistro = 'A'")
    Optional<MetodoPago> findPredeterminado(@Param("idCuenta") Long idCuenta);
}
