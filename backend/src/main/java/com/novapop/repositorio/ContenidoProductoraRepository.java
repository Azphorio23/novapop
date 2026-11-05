package com.novapop.repositorio;

import com.novapop.entidad.ContenidoProductora;
import com.novapop.entidad.ContenidoProductoraId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContenidoProductoraRepository extends JpaRepository<ContenidoProductora, ContenidoProductoraId> {

    @Query("""
            select cp from ContenidoProductora cp join fetch cp.productora
             where cp.id.idContenido = :idContenido and cp.estadoRegistro = 'A'
             order by cp.esProductoraPrincipal desc
            """)
    List<ContenidoProductora> findPorContenido(@Param("idContenido") Long idContenido);
}
