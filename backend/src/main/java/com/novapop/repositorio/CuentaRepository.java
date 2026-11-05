package com.novapop.repositorio;

import com.novapop.entidad.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    @Query("select c from Cuenta c join fetch c.pais where lower(c.correo) = lower(:correo) and c.estadoRegistro = 'A'")
    Optional<Cuenta> findPorCorreo(@Param("correo") String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    @Query("select c from Cuenta c join fetch c.pais where c.idCuenta = :id")
    Optional<Cuenta> findConPais(@Param("id") Long idCuenta);
}
