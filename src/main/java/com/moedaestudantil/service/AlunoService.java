package com.moedaestudantil.service;

import com.moedaestudantil.dto.AlunoForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.InstituicaoEnsino;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.InstituicaoEnsinoRepository;
import com.moedaestudantil.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Serviço responsável pelas regras de negócio do CRUD de Aluno.
 * Controller → Service (@Transactional) → Repository.
 */
@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final InstituicaoEnsinoRepository instituicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository,
                        InstituicaoEnsinoRepository instituicaoRepository,
                        UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Aluno não encontrado: " + id));
    }

    /**
     * Cria Usuario + Aluno com senha em BCrypt e perfil ALUNO.
     * Rejeita CPF ou e-mail duplicado com RegraDeNegocioException.
     */
    @Transactional
    public Aluno criar(AlunoForm form) {
        validarLogin(form.getLogin(), null);
        validarCpf(form.getCpf(), null);
        validarEmail(form.getEmail(), null);

        InstituicaoEnsino instituicao = buscarInstituicao(form.getInstituicaoId());

        Aluno aluno = new Aluno();
        aluno.setLogin(form.getLogin());
        aluno.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        aluno.setNome(form.getNome());
        aluno.setEmail(form.getEmail());
        aluno.setCpf(form.getCpf());
        aluno.setRg(form.getRg());
        aluno.setEndereco(form.getEndereco());
        aluno.setCurso(form.getCurso());
        aluno.setInstituicao(instituicao);

        return alunoRepository.save(aluno);
    }

    /**
     * Atualiza dados do Aluno. Não altera saldo, login nem perfil.
     */
    @Transactional
    public Aluno atualizar(Long id, AlunoForm form) {
        Aluno aluno = buscarPorId(id);

        validarCpf(form.getCpf(), id);
        validarEmail(form.getEmail(), id);

        InstituicaoEnsino instituicao = buscarInstituicao(form.getInstituicaoId());

        aluno.setNome(form.getNome());
        aluno.setEmail(form.getEmail());
        aluno.setCpf(form.getCpf());
        aluno.setRg(form.getRg());
        aluno.setEndereco(form.getEndereco());
        aluno.setCurso(form.getCurso());
        aluno.setInstituicao(instituicao);
        // saldo, login e perfil NÃO são alterados

        return alunoRepository.save(aluno);
    }

    /**
     * Exclui aluno. Bloqueado se houver transações associadas.
     */
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = buscarPorId(id);

        if (alunoRepository.temEnviosRecebidos(id) || alunoRepository.temResgates(id)) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir o aluno pois ele possui transações registradas no sistema.");
        }

        alunoRepository.delete(aluno);
    }

    // ---- validações internas ----

    private void validarLogin(String login, Long idExcluir) {
        if (login == null || login.isBlank()) {
            throw new RegraDeNegocioException("Login é obrigatório.");
        }
        boolean existe = (idExcluir == null)
                ? usuarioRepository.existsByLogin(login)
                : usuarioRepository.findByLogin(login)
                    .filter(u -> !u.getId().equals(idExcluir))
                    .isPresent();
        if (existe) {
            throw new RegraDeNegocioException("Login '" + login + "' já está em uso.");
        }
    }

    private void validarCpf(String cpf, Long idExcluir) {
        boolean existe = (idExcluir == null)
                ? alunoRepository.existsByCpf(cpf)
                : alunoRepository.existsByCpfAndIdNot(cpf, idExcluir);
        if (existe) {
            throw new RegraDeNegocioException("CPF já cadastrado no sistema.");
        }
    }

    private void validarEmail(String email, Long idExcluir) {
        boolean existe = (idExcluir == null)
                ? alunoRepository.existsByEmail(email)
                : alunoRepository.existsByEmailAndIdNot(email, idExcluir);
        if (existe) {
            throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
        }
    }

    private InstituicaoEnsino buscarInstituicao(Long id) {
        return instituicaoRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Instituição não encontrada."));
    }
}
