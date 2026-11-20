package com.moedaestudantil.service;

import com.moedaestudantil.dto.VantagemForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.model.Vantagem;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import com.moedaestudantil.repository.VantagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de vantagens. Toda operação de escrita é restrita à empresa autenticada dona da vantagem.
 */
@Service
public class VantagemService {

    private final VantagemRepository vantagemRepository;
    private final EmpresaParceiraRepository empresaRepository;
    private final FotoStorageService fotoStorage;

    public VantagemService(VantagemRepository vantagemRepository,
                           EmpresaParceiraRepository empresaRepository,
                           FotoStorageService fotoStorage) {
        this.vantagemRepository = vantagemRepository;
        this.empresaRepository = empresaRepository;
        this.fotoStorage = fotoStorage;
    }

    @Transactional(readOnly = true)
    public List<Vantagem> listarDaEmpresa(String loginEmpresa) {
        return vantagemRepository.findByEmpresaIdOrderByNomeAsc(buscarEmpresa(loginEmpresa).getId());
    }

    /** Catálogo exibido ao aluno. */
    @Transactional(readOnly = true)
    public List<Vantagem> listarCatalogo() {
        return vantagemRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Vantagem buscarDaEmpresa(String loginEmpresa, Long id) {
        EmpresaParceira empresa = buscarEmpresa(loginEmpresa);
        Vantagem vantagem = vantagemRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Vantagem não encontrada."));
        if (!vantagem.getEmpresa().getId().equals(empresa.getId())) {
            throw new RegraDeNegocioException("Esta vantagem pertence a outra empresa.");
        }
        return vantagem;
    }

    @Transactional
    public Vantagem criar(String loginEmpresa, VantagemForm form) {
        if (form.getFoto() == null || form.getFoto().isEmpty()) {
            throw new RegraDeNegocioException("A foto da vantagem é obrigatória.");
        }
        Vantagem vantagem = new Vantagem();
        vantagem.setEmpresa(buscarEmpresa(loginEmpresa));
        preencher(vantagem, form);
        vantagem.setFotoUrl(fotoStorage.salvar(form.getFoto()));
        return vantagemRepository.save(vantagem);
    }

    @Transactional
    public Vantagem atualizar(String loginEmpresa, Long id, VantagemForm form) {
        Vantagem vantagem = buscarDaEmpresa(loginEmpresa, id);
        preencher(vantagem, form);
        if (form.getFoto() != null && !form.getFoto().isEmpty()) {
            vantagem.setFotoUrl(fotoStorage.salvar(form.getFoto()));
        }
        return vantagemRepository.save(vantagem);
    }

    @Transactional
    public void excluir(String loginEmpresa, Long id) {
        Vantagem vantagem = buscarDaEmpresa(loginEmpresa, id);
        if (vantagemRepository.temResgates(id)) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir a vantagem pois ela já foi resgatada por alunos.");
        }
        vantagemRepository.delete(vantagem);
    }

    private void preencher(Vantagem vantagem, VantagemForm form) {
        vantagem.setNome(form.getNome().trim());
        vantagem.setDescricao(form.getDescricao().trim());
        vantagem.setCustoMoedas(form.getCustoMoedas());
    }

    private EmpresaParceira buscarEmpresa(String login) {
        return empresaRepository.findByLogin(login)
                .orElseThrow(() -> new RegraDeNegocioException("Empresa não encontrada."));
    }
}
