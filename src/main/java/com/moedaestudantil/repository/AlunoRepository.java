package com.moedaestudantil.repository;

import com.moedaestudantil.model.Aluno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByCpf(String cpf);

    Optional<Aluno> findByEmail(String email);

    Optional<Aluno> findByLogin(String login);

    List<Aluno> findByInstituicaoIdOrderByNomeAsc(Long instituicaoId);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    /** Carrega o aluno com lock de escrita para creditar/debitar saldo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Aluno a WHERE a.id = :id")
    Optional<Aluno> buscarParaAtualizar(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Aluno a WHERE a.login = :login")
    Optional<Aluno> buscarParaAtualizarPorLogin(@Param("login") String login);

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
