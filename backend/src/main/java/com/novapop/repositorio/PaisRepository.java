package com.novapop.repositorio;

import com.novapop.entidad.Pais;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaisRepository extends JpaRepository<Pais, Long> {

    List<Pais> findByEstadoRegistroOrderByNombrePais(String estadoRegistro);
}
