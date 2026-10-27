package com.moedaestudantil.repository;

import com.moedaestudantil.model.InstituicaoEnsino;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Teste de repositório — usa slice @DataJpaTest com H2 em memória.
 */
@DataJpaTest
@ActiveProfiles("dev")
@DisplayName("InstituicaoEnsinoRepository — teste de repositório")
class InstituicaoEnsinoRepositoryTest {

    @Autowired
    InstituicaoEnsinoRepository repository;

    @Test
    @DisplayName("Salvar e buscar instituição por nome")
    void salvarEBuscarPorNome() {
        // arrange
        InstituicaoEnsino inst = new InstituicaoEnsino("Universidade de Teste");
        repository.save(inst);

        // act
        Optional<InstituicaoEnsino> encontrada = repository.findByNome("Universidade de Teste");

        // assert
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Universidade de Teste");
    }

    @Test
    @DisplayName("existsByNome retorna true para nome existente")
    void existsByNome_retornaTrue() {
        repository.save(new InstituicaoEnsino("UFMG"));
        assertThat(repository.existsByNome("UFMG")).isTrue();
    }

    @Test
    @DisplayName("existsByNome retorna false para nome inexistente")
    void existsByNome_retornaFalse() {
        assertThat(repository.existsByNome("Faculdade Inexistente")).isFalse();
    }
}
