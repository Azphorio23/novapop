package com.novapop.repositorio;

import com.novapop.entidad.Idioma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdiomaRepository extends JpaRepository<Idioma, Long> {

    Optional<Idioma> findByCodigoIso(String codigoIso);

    List<Idioma> findByEstadoRegistroOrderByNombreIdioma(String estadoRegistro);
}
