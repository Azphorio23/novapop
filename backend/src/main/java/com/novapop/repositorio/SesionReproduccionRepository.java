package com.novapop.repositorio;

import com.novapop.entidad.SesionReproduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SesionReproduccionRepository extends JpaRepository<SesionReproduccion, Long> {

    /** Sesiones abiertas de la cuenta (sin fecha_fin) con actividad posterior a "desde". */
    @Query("""
            select s from SesionReproduccion s join fetch s.dispositivo d
             where d.cuenta.idCuenta = :idCuenta and s.fechaFin is null and s.estadoRegistro = 'A'
               and coalesce(s.fechaModificacion, s.fechaInicio) >= :desde
            """)
    List<SesionReproduccion> findActivasDeCuenta(@Param("idCuenta") Long idCuenta, @Param("desde") OffsetDateTime desde);

    @Query("select s from SesionReproduccion s where s.dispositivo.idDispositivo = :idDispositivo and s.fechaFin is null")
    List<SesionReproduccion> findAbiertasDeDispositivo(@Param("idDispositivo") Long idDispositivo);
}
