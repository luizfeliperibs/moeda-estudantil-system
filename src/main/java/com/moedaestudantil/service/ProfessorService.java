package com.moedaestudantil.service;

import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consulta de professor e crédito semestral de moedas.
 */
@Service
public class ProfessorService {

    public static final int CREDITO_SEMESTRAL = 1000;

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;

    public ProfessorService(ProfessorRepository professorRepository,
                            AlunoRepository alunoRepository) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
    }

    @Transactional(readOnly = true)
    public Professor buscarPorLogin(String login) {
        return professorRepository.findByLogin(login)
                .orElseThrow(() -> new RegraDeNegocioException("Professor não encontrado."));
    }

    /** Alunos que o professor pode premiar: os da sua própria instituição. */
    @Transactional(readOnly = true)
    public List<Aluno> listarAlunosElegiveis(String login) {
        Professor professor = buscarPorLogin(login);
        return alunoRepository.findByInstituicaoIdOrderByNomeAsc(professor.getInstituicao().getId());
    }

    /** Soma {@value #CREDITO_SEMESTRAL} moedas ao saldo de cada professor (acumulável). */
    @Transactional
    public int creditarSemestre() {
        var professores = professorRepository.findAll();
        professores.forEach(p -> p.setSaldo(p.getSaldo() + CREDITO_SEMESTRAL));
        professorRepository.saveAll(professores);
        return professores.size();
    }
}
