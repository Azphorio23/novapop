package com.novapop.repositorio;

import com.novapop.entidad.ContenidoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContenidoPersonaRepository extends JpaRepository<ContenidoPersona, Long> {

    @Query("""
            select cp from ContenidoPersona cp join fetch cp.persona join fetch cp.rolProduccion
             where cp.contenido.idContenido = :idContenido and cp.estadoRegistro = 'A'
             order by cp.ordenCreditos
            """)
    List<ContenidoPersona> findCreditos(@Param("idContenido") Long idContenido);
}
