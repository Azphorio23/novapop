package com.novapop.repositorio;

import com.novapop.entidad.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    @Query("""
            select p from Perfil p join fetch p.idiomaPreferido
             where p.cuenta.idCuenta = :idCuenta and p.estadoRegistro = 'A'
             order by p.idPerfil
            """)
    List<Perfil> findActivos(@Param("idCuenta") Long idCuenta);

    @Query("""
            select p from Perfil p join fetch p.cuenta c join fetch c.pais join fetch p.idiomaPreferido
             where p.idPerfil = :idPerfil and c.idCuenta = :idCuenta and p.estadoRegistro = 'A'
            """)
    Optional<Perfil> findDeCuenta(@Param("idCuenta") Long idCuenta, @Param("idPerfil") Long idPerfil);

    @Query("select count(p) from Perfil p where p.cuenta.idCuenta = :idCuenta and p.estadoRegistro = 'A'")
    long contarActivos(@Param("idCuenta") Long idCuenta);

    @Query("""
            select count(p) > 0 from Perfil p
             where p.cuenta.idCuenta = :idCuenta and lower(p.nombrePerfil) = lower(:nombre)
               and p.estadoRegistro = 'A' and p.idPerfil <> :excluirId
            """)
    boolean existeNombre(@Param("idCuenta") Long idCuenta, @Param("nombre") String nombre,
                         @Param("excluirId") Long excluirId);
}
