package com.moedaestudantil.repository;

import com.moedaestudantil.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByCpf(String cpf);

    Optional<Aluno> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    /** Verifica se o aluno possui qualquer transação (envio recebido ou resgate). */
    @Query("""
        SELECT COUNT(e) > 0
        FROM EnvioMoedas e
        WHERE e.aluno.id = :alunoId
    """)
    boolean temEnviosRecebidos(@Param("alunoId") Long alunoId);

    @Query("""
        SELECT COUNT(r) > 0
        FROM ResgateVantagem r
        WHERE r.aluno.id = :alunoId
    """)
    boolean temResgates(@Param("alunoId") Long alunoId);
}
