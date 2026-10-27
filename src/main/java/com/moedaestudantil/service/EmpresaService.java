package com.moedaestudantil.service;

import com.moedaestudantil.dto.EmpresaForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import com.moedaestudantil.repository.VantagemRepository;
import com.moedaestudantil.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Serviço responsável pelas regras de negócio do CRUD de EmpresaParceira.
 */
@Service
public class EmpresaService {

    private final EmpresaParceiraRepository empresaRepository;
    private final VantagemRepository vantagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpresaService(EmpresaParceiraRepository empresaRepository,
                          VantagemRepository vantagemRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.vantagemRepository = vantagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmpresaParceira> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EmpresaParceira buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Empresa não encontrada: " + id));
    }

    /**
     * Cria Usuario + EmpresaParceira com perfil EMPRESA, rejeitando CNPJ duplicado.
     */
    @Transactional
    public EmpresaParceira criar(EmpresaForm form) {
        validarLogin(form.getLogin());
        validarCnpj(form.getCnpj(), null);

        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setLogin(form.getLogin());
        empresa.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        empresa.setNome(form.getNome());
        empresa.setCnpj(form.getCnpj());
        empresa.setEmail(form.getEmail());

        return empresaRepository.save(empresa);
    }

    /**
     * Atualiza dados da empresa. Não altera login nem perfil.
     */
    @Transactional
    public EmpresaParceira atualizar(Long id, EmpresaForm form) {
        EmpresaParceira empresa = buscarPorId(id);

        validarCnpj(form.getCnpj(), id);

        empresa.setNome(form.getNome());
        empresa.setCnpj(form.getCnpj());
        empresa.setEmail(form.getEmail());

        return empresaRepository.save(empresa);
    }

    /**
     * Exclui empresa. Bloqueado se houver vantagens resgatadas.
     */
    @Transactional
    public void excluir(Long id) {
        EmpresaParceira empresa = buscarPorId(id);

        if (vantagemRepository.empresaTemVantagensResgatadas(id)) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir a empresa pois ela possui vantagens que já foram resgatadas.");
        }

        empresaRepository.delete(empresa);
    }

    // ---- validações internas ----

    private void validarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new RegraDeNegocioException("Login é obrigatório.");
        }
        if (usuarioRepository.existsByLogin(login)) {
            throw new RegraDeNegocioException("Login '" + login + "' já está em uso.");
        }
    }

    private void validarCnpj(String cnpj, Long idExcluir) {
        boolean existe = (idExcluir == null)
                ? empresaRepository.existsByCnpj(cnpj)
                : empresaRepository.existsByCnpjAndIdNot(cnpj, idExcluir);
        if (existe) {
            throw new RegraDeNegocioException("CNPJ já cadastrado no sistema.");
        }
    }
}
