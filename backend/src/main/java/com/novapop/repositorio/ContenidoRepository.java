package com.novapop.repositorio;

import com.novapop.entidad.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ContenidoRepository extends JpaRepository<Contenido, Long> {

    /** Contenido con licencia vigente en el país y apto para la edad indicada. */
    @Query("""
            select c from Contenido c
              join fetch c.clasificacion cl
              join fetch c.idiomaOriginal
             where c.estadoRegistro = 'A'
               and cl.edadMinima <= :edadMaxima
               and exists (select 1 from ContenidoPais cp
                            where cp.contenido = c and cp.pais.idPais = :idPais and cp.estadoRegistro = 'A'
                              and cp.fechaDisponibleDesde <= :hoy
                              and (cp.fechaDisponibleHasta is null or cp.fechaDisponibleHasta >= :hoy))
             order by c.anioLanzamiento desc, c.idContenido
            """)
    List<Contenido> findDisponibles(@Param("idPais") Long idPais, @Param("hoy") LocalDate hoy,
                                    @Param("edadMaxima") short edadMaxima);

    /** Contenido cuya licencia en el país empieza más adelante ("Próximamente"). */
    @Query("""
            select c from Contenido c
              join fetch c.clasificacion cl
              join ContenidoPais cp on cp.contenido = c
             where c.estadoRegistro = 'A' and cp.estadoRegistro = 'A'
               and cl.edadMinima <= :edadMaxima
               and cp.pais.idPais = :idPais
               and cp.fechaDisponibleDesde > :hoy
             order by cp.fechaDisponibleDesde
            """)
    List<Contenido> findProximamente(@Param("idPais") Long idPais, @Param("hoy") LocalDate hoy,
                                     @Param("edadMaxima") short edadMaxima);
}
