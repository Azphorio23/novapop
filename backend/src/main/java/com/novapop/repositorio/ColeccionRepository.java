package com.novapop.repositorio;

import com.novapop.entidad.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColeccionRepository extends JpaRepository<Coleccion, Long> {

    List<Coleccion> findByEstadoRegistroOrderByOrdenVitrina(String estadoRegistro);
}
