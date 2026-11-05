package com.novapop.repositorio;

import com.novapop.entidad.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {

    @Query("""
            select d from Dispositivo d
             where d.cuenta.idCuenta = :idCuenta and d.estadoRegistro = 'A'
             order by d.ultimaConexion desc nulls last
            """)
    List<Dispositivo> findActivos(@Param("idCuenta") Long idCuenta);

    @Query("select d from Dispositivo d where d.idDispositivo = :id and d.cuenta.idCuenta = :idCuenta and d.estadoRegistro = 'A'")
    Optional<Dispositivo> findDeCuenta(@Param("idCuenta") Long idCuenta, @Param("id") Long idDispositivo);
}
