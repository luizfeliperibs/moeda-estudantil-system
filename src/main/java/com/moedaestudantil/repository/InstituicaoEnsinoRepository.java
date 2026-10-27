package com.moedaestudantil.repository;

import com.moedaestudantil.model.InstituicaoEnsino;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstituicaoEnsinoRepository extends JpaRepository<InstituicaoEnsino, Long> {
    Optional<InstituicaoEnsino> findByNome(String nome);
    boolean existsByNome(String nome);
}
