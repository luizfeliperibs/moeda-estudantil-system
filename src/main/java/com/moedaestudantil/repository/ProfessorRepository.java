package com.moedaestudantil.repository;

import com.moedaestudantil.model.Professor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByCpf(String cpf);
    boolean existsByCpf(String cpf);

    Optional<Professor> findByLogin(String login);

    /** Carrega o professor com lock de escrita, evitando envios concorrentes com o mesmo saldo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Professor p WHERE p.login = :login")
    Optional<Professor> buscarParaAtualizarPorLogin(@Param("login") String login);
}
