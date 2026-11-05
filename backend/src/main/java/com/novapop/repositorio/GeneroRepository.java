package com.novapop.repositorio;

import com.novapop.entidad.Genero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneroRepository extends JpaRepository<Genero, Long> {

    List<Genero> findByEstadoRegistroOrderByNombreGenero(String estadoRegistro);
}
