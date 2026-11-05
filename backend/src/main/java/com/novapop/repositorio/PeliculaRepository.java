package com.novapop.repositorio;

import com.novapop.entidad.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    @Query("select p from Pelicula p left join fetch p.video where p.idContenido in :ids")
    List<Pelicula> findConVideo(@Param("ids") Collection<Long> idsContenido);
}
