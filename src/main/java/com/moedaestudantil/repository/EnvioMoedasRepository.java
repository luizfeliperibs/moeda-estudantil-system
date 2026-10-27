package com.moedaestudantil.repository;

import com.moedaestudantil.model.EnvioMoedas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnvioMoedasRepository extends JpaRepository<EnvioMoedas, Long> {
    List<EnvioMoedas> findByAlunoId(Long alunoId);
    List<EnvioMoedas> findByProfessorId(Long professorId);
}
