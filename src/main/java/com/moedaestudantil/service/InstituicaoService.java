package com.moedaestudantil.service;

import com.moedaestudantil.model.InstituicaoEnsino;
import com.moedaestudantil.repository.InstituicaoEnsinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InstituicaoService {

    private final InstituicaoEnsinoRepository repository;

    public InstituicaoService(InstituicaoEnsinoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoEnsino> listarTodas() {
        return repository.findAll();
    }
}
