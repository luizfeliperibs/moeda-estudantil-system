package com.moedaestudantil.service;

import com.moedaestudantil.dto.ExtratoItem;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.EnvioMoedasRepository;
import com.moedaestudantil.repository.ProfessorRepository;
import com.moedaestudantil.repository.ResgateVantagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Monta o extrato de professores (envios) e alunos (recebimentos e resgates),
 * do mais recente para o mais antigo.
 */
@Service
@Transactional(readOnly = true)
public class ExtratoService {

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final EnvioMoedasRepository envioRepository;
    private final ResgateVantagemRepository resgateRepository;

    public ExtratoService(ProfessorRepository professorRepository,
                          AlunoRepository alunoRepository,
                          EnvioMoedasRepository envioRepository,
                          ResgateVantagemRepository resgateRepository) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.envioRepository = envioRepository;
        this.resgateRepository = resgateRepository;
    }

    public List<ExtratoItem> extratoProfessor(String login) {
        Professor professor = professorRepository.findByLogin(login)
                .orElseThrow(() -> new RegraDeNegocioException("Professor não encontrado."));
        return envioRepository.findByProfessorIdOrderByDataHoraDesc(professor.getId()).stream()
                .map(e -> new ExtratoItem(e.getDataHora(), "ENVIO",
                        "Para " + e.getAluno().getNome() + ": " + e.getMensagem(), -e.getValor()))
                .toList();
    }

    public List<ExtratoItem> extratoAluno(String login) {
        Aluno aluno = alunoRepository.findByLogin(login)
                .orElseThrow(() -> new RegraDeNegocioException("Aluno não encontrado."));

        List<ExtratoItem> itens = new ArrayList<>();
        envioRepository.findByAlunoIdOrderByDataHoraDesc(aluno.getId()).forEach(e ->
                itens.add(new ExtratoItem(e.getDataHora(), "RECEBIDO",
                        "De " + e.getProfessor().getNome() + ": " + e.getMensagem(), e.getValor())));
        resgateRepository.findByAlunoIdOrderByDataHoraDesc(aluno.getId()).forEach(r ->
                itens.add(new ExtratoItem(r.getDataHora(), "RESGATE",
                        r.getVantagem().getNome() + " (código " + r.getCodigo() + ")", -r.getValor())));
        itens.sort(Comparator.comparing(ExtratoItem::dataHora).reversed());
        return itens;
    }
}
