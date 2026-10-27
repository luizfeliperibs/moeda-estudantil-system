package com.moedaestudantil.repository;

import com.moedaestudantil.model.ResgateVantagem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ResgateVantagemRepository extends JpaRepository<ResgateVantagem, Long> {
    Optional<ResgateVantagem> findByCodigo(String codigo);
    List<ResgateVantagem> findByAlunoId(Long alunoId);
}
