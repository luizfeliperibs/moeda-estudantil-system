package com.moedaestudantil.repository;

import com.moedaestudantil.model.Vantagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {

    List<Vantagem> findByEmpresaId(Long empresaId);

    /** Verifica se a empresa tem vantagens já resgatadas (bloqueio de exclusão). */
    @Query("""
        SELECT COUNT(r) > 0
        FROM ResgateVantagem r
        WHERE r.vantagem.empresa.id = :empresaId
    """)
    boolean empresaTemVantagensResgatadas(@Param("empresaId") Long empresaId);
}
