package com.novapop.repositorio;

import com.novapop.entidad.ColeccionContenido;
import com.novapop.entidad.ColeccionContenidoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ColeccionContenidoRepository extends JpaRepository<ColeccionContenido, ColeccionContenidoId> {

    @Query("select cc from ColeccionContenido cc where cc.estadoRegistro = 'A' order by cc.id.idColeccion, cc.ordenAparicion")
    List<ColeccionContenido> findActivosOrdenados();
}
