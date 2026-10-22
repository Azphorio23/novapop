package com.novapop.repositorio;

import com.novapop.entidad.MiLista;
import com.novapop.entidad.MiListaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MiListaRepository extends JpaRepository<MiLista, MiListaId> {

    @Query("""
            select m.id.idContenido from MiLista m
             where m.id.idPerfil = :idPerfil and m.estadoRegistro = 'A'
             order by m.fechaAgregado desc
            """)
    List<Long> findIdsContenido(@Param("idPerfil") Long idPerfil);
}
