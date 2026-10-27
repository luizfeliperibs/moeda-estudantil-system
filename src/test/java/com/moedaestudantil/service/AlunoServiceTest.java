package com.moedaestudantil.service;

import com.moedaestudantil.dto.AlunoForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.model.InstituicaoEnsino;
import com.moedaestudantil.repository.AlunoRepository;
import com.moedaestudantil.repository.InstituicaoEnsinoRepository;
import com.moedaestudantil.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlunoService — testes de serviço")
class AlunoServiceTest {

    @Mock AlunoRepository alunoRepository;
    @Mock InstituicaoEnsinoRepository instituicaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AlunoService alunoService;

    private AlunoForm formValido;
    private InstituicaoEnsino instituicao;

    @BeforeEach
    void setUp() {
        instituicao = new InstituicaoEnsino("PUC Minas");

        formValido = new AlunoForm();
        formValido.setNome("Maria Teste");
        formValido.setEmail("maria@teste.com");
        formValido.setCpf("111.222.333-44");
        formValido.setRg("MG-1234567");
        formValido.setEndereco("Rua Teste, 1");
        formValido.setCurso("Engenharia de Software");
        formValido.setInstituicaoId(1L);
        formValido.setLogin("maria.teste");
        formValido.setSenha("senha123");
    }

    @Test
    @DisplayName("Criar aluno — caso feliz")
    void criarAluno_casoFeliz() {
        // arrange
        when(usuarioRepository.existsByLogin("maria.teste")).thenReturn(false);
        when(alunoRepository.existsByCpf("111.222.333-44")).thenReturn(false);
        when(alunoRepository.existsByEmail("maria@teste.com")).thenReturn(false);
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(instituicao));
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$hashed");

        Aluno alunoSalvo = new Aluno();
        alunoSalvo.setNome("Maria Teste");
        when(alunoRepository.save(any(Aluno.class))).thenReturn(alunoSalvo);

        // act
        Aluno resultado = alunoService.criar(formValido);

        // assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Maria Teste");
        verify(alunoRepository, times(1)).save(any(Aluno.class));
    }

    @Test
    @DisplayName("Criar aluno — CPF duplicado deve lançar exceção")
    void criarAluno_cpfDuplicado() {
        // arrange
        when(usuarioRepository.existsByLogin("maria.teste")).thenReturn(false);
        when(alunoRepository.existsByCpf("111.222.333-44")).thenReturn(true); // CPF já existe

        // act & assert
        assertThatThrownBy(() -> alunoService.criar(formValido))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(alunoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar aluno — e-mail duplicado deve lançar exceção")
    void criarAluno_emailDuplicado() {
        // arrange
        when(usuarioRepository.existsByLogin("maria.teste")).thenReturn(false);
        when(alunoRepository.existsByCpf("111.222.333-44")).thenReturn(false);
        when(alunoRepository.existsByEmail("maria@teste.com")).thenReturn(true); // e-mail já existe

        // act & assert
        assertThatThrownBy(() -> alunoService.criar(formValido))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("E-mail já cadastrado");

        verify(alunoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Excluir aluno — bloqueado se tiver transações")
    void excluirAluno_comTransacoes_deveLancarExcecao() {
        // arrange
        Aluno aluno = new Aluno();
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.temEnviosRecebidos(1L)).thenReturn(true);

        // act & assert
        assertThatThrownBy(() -> alunoService.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("transações");

        verify(alunoRepository, never()).delete(any());
    }
}
