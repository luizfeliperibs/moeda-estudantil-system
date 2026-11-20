package com.moedaestudantil.service;

import com.moedaestudantil.dto.EnvioMoedasForm;
import com.moedaestudantil.event.EnvioMoedasRealizado;
import com.moedaestudantil.event.ResgateRealizado;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Regras de movimentação de moedas: envio (professor → aluno) e resgate de vantagem.
 * Débito, crédito e registro da transação ocorrem na mesma transação do banco;
 * os e-mails são publicados como eventos e saem apenas após o commit.
 */
@Service
public class TransacaoService {

    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_CODIGO = 8;
    private static final int TENTATIVAS_CODIGO = 5;

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final EnvioMoedasRepository envioRepository;
    private final ResgateVantagemRepository resgateRepository;
    private final ApplicationEventPublisher eventos;
    private final SecureRandom random = new SecureRandom();

    public TransacaoService(ProfessorRepository professorRepository,
                            AlunoRepository alunoRepository,
                            VantagemRepository vantagemRepository,
                            EnvioMoedasRepository envioRepository,
                            ResgateVantagemRepository resgateRepository,
                            ApplicationEventPublisher eventos) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.vantagemRepository = vantagemRepository;
        this.envioRepository = envioRepository;
        this.resgateRepository = resgateRepository;
        this.eventos = eventos;
    }

    /**
     * Envia moedas do professor autenticado para um aluno da mesma instituição.
     * Exige valor positivo, mensagem não vazia e saldo suficiente.
     */
    @Transactional
    public EnvioMoedas enviarMoedas(String loginProfessor, EnvioMoedasForm form) {
        if (form.getValor() == null || form.getValor() < 1) {
            throw new RegraDeNegocioException("A quantidade de moedas deve ser maior que zero.");
        }
        if (form.getMensagem() == null || form.getMensagem().isBlank()) {
            throw new RegraDeNegocioException("O motivo do envio é obrigatório.");
        }

        Professor professor = professorRepository.buscarParaAtualizarPorLogin(loginProfessor)
                .orElseThrow(() -> new RegraDeNegocioException("Professor não encontrado."));
        Aluno aluno = alunoRepository.buscarParaAtualizar(form.getAlunoId())
                .orElseThrow(() -> new RegraDeNegocioException("Aluno não encontrado."));

        if (!aluno.getInstituicao().getId().equals(professor.getInstituicao().getId())) {
            throw new RegraDeNegocioException("O aluno não pertence à sua instituição de ensino.");
        }

        int valor = form.getValor();
        if (professor.getSaldo() < valor) {
            throw new RegraDeNegocioException(
                    "Saldo insuficiente: você tem " + professor.getSaldo() + " moedas.");
        }

        professor.setSaldo(professor.getSaldo() - valor);
        aluno.setSaldo(aluno.getSaldo() + valor);
        professorRepository.save(professor);
        alunoRepository.save(aluno);

        EnvioMoedas envio = new EnvioMoedas();
        envio.setProfessor(professor);
        envio.setAluno(aluno);
        envio.setValor(valor);
        envio.setMensagem(form.getMensagem().trim());
        EnvioMoedas salvo = envioRepository.save(envio);

        eventos.publishEvent(new EnvioMoedasRealizado(
                aluno.getNome(), aluno.getEmail(), professor.getNome(), valor, salvo.getMensagem()));
        return salvo;
    }

    /**
     * Troca moedas do aluno autenticado por uma vantagem, gerando um código único.
     */
    @Transactional
    public ResgateVantagem resgatar(String loginAluno, Long vantagemId) {
        Aluno aluno = alunoRepository.buscarParaAtualizarPorLogin(loginAluno)
                .orElseThrow(() -> new RegraDeNegocioException("Aluno não encontrado."));
        Vantagem vantagem = vantagemRepository.findById(vantagemId)
                .orElseThrow(() -> new RegraDeNegocioException("Vantagem não encontrada."));

        int custo = vantagem.getCustoMoedas();
        if (aluno.getSaldo() < custo) {
            throw new RegraDeNegocioException(
                    "Saldo insuficiente: você tem " + aluno.getSaldo() + " moedas e a vantagem custa " + custo + ".");
        }

        aluno.setSaldo(aluno.getSaldo() - custo);
        alunoRepository.save(aluno);

        ResgateVantagem resgate = new ResgateVantagem();
        resgate.setAluno(aluno);
        resgate.setVantagem(vantagem);
        resgate.setValor(custo);
        resgate.setCodigo(gerarCodigoUnico());
        ResgateVantagem salvo = resgateRepository.save(resgate);

        EmpresaParceira empresa = vantagem.getEmpresa();
        eventos.publishEvent(new ResgateRealizado(
                aluno.getNome(), aluno.getEmail(), empresa.getNome(), empresa.getEmail(),
                vantagem.getNome(), custo, salvo.getCodigo()));
        return salvo;
    }

    private String gerarCodigoUnico() {
        for (int i = 0; i < TENTATIVAS_CODIGO; i++) {
            StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
            for (int j = 0; j < TAMANHO_CODIGO; j++) {
                sb.append(ALFABETO_CODIGO.charAt(random.nextInt(ALFABETO_CODIGO.length())));
            }
            String codigo = sb.toString();
            if (!resgateRepository.existsByCodigo(codigo)) {
                return codigo;
            }
        }
        throw new RegraDeNegocioException("Não foi possível gerar o código do cupom. Tente novamente.");
    }
}
