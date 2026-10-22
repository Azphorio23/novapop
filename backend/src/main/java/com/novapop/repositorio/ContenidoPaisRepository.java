package com.novapop.repositorio;

import com.novapop.entidad.ContenidoPais;
import com.novapop.entidad.ContenidoPaisId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContenidoPaisRepository extends JpaRepository<ContenidoPais, ContenidoPaisId> {
}
