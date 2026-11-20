package com.moedaestudantil.service;

import com.moedaestudantil.dto.EnvioMoedasForm;
import com.moedaestudantil.event.EnvioMoedasRealizado;
import com.moedaestudantil.event.ResgateRealizado;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService — envio de moedas e resgate")
class TransacaoServiceTest {

    @Mock ProfessorRepository professorRepository;
    @Mock AlunoRepository alunoRepository;
    @Mock VantagemRepository vantagemRepository;
    @Mock EnvioMoedasRepository envioRepository;
    @Mock ResgateVantagemRepository resgateRepository;
    @Mock ApplicationEventPublisher eventos;

    @InjectMocks TransacaoService service;

    private InstituicaoEnsino puc;
    private Professor professor;
    private Aluno aluno;
    private EnvioMoedasForm form;

    @BeforeEach
    void setUp() {
        puc = new InstituicaoEnsino("PUC Minas");
        puc.setId(1L);

        professor = new Professor("Milena", "111.111.111-11", "ES", puc);
        professor.setLogin("milena");
        professor.setSaldo(100);

        aluno = new Aluno();
        aluno.setNome("Ana");
        aluno.setEmail("ana@aluno.com");
        aluno.setInstituicao(puc);
        aluno.setSaldo(5);

        form = new EnvioMoedasForm();
        form.setAlunoId(7L);
        form.setValor(40);
        form.setMensagem("Ótimo trabalho");
    }

    private void stubProfessorEAluno() {
        when(professorRepository.buscarParaAtualizarPorLogin("milena")).thenReturn(Optional.of(professor));
        when(alunoRepository.buscarParaAtualizar(7L)).thenReturn(Optional.of(aluno));
    }

    @Test
    @DisplayName("Envio — caso feliz debita professor, credita aluno e publica evento")
    void enviar_casoFeliz() {
        stubProfessorEAluno();
        when(envioRepository.save(any(EnvioMoedas.class))).thenAnswer(i -> i.getArgument(0));

        EnvioMoedas envio = service.enviarMoedas("milena", form);

        assertThat(professor.getSaldo()).isEqualTo(60);
        assertThat(aluno.getSaldo()).isEqualTo(45);
        assertThat(envio.getValor()).isEqualTo(40);
        assertThat(envio.getMensagem()).isEqualTo("Ótimo trabalho");
        verify(eventos).publishEvent(any(EnvioMoedasRealizado.class));
    }

    @Test
    @DisplayName("Envio — saldo insuficiente é rejeitado e nada muda")
    void enviar_saldoInsuficiente() {
        form.setValor(150);
        stubProfessorEAluno();

        assertThatThrownBy(() -> service.enviarMoedas("milena", form))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(professor.getSaldo()).isEqualTo(100);
        assertThat(aluno.getSaldo()).isEqualTo(5);
        verify(envioRepository, never()).save(any());
        verify(eventos, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Envio — mensagem em branco é rejeitada")
    void enviar_mensagemObrigatoria() {
        form.setMensagem("   ");

        assertThatThrownBy(() -> service.enviarMoedas("milena", form))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("motivo");
        verifyNoInteractions(professorRepository, envioRepository);
    }

    @Test
    @DisplayName("Envio — valor zero ou negativo é rejeitado")
    void enviar_valorInvalido() {
        form.setValor(0);

        assertThatThrownBy(() -> service.enviarMoedas("milena", form))
                .isInstanceOf(RegraDeNegocioException.class);
        verifyNoInteractions(professorRepository, envioRepository);
    }

    @Test
    @DisplayName("Envio — aluno de outra instituição é rejeitado")
    void enviar_outraInstituicao() {
        InstituicaoEnsino outra = new InstituicaoEnsino("UFMG");
        outra.setId(2L);
        aluno.setInstituicao(outra);
        stubProfessorEAluno();

        assertThatThrownBy(() -> service.enviarMoedas("milena", form))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("instituição");
        assertThat(professor.getSaldo()).isEqualTo(100);
    }

    @Test
    @DisplayName("Resgate — caso feliz debita aluno, gera código e publica evento")
    void resgatar_casoFeliz() {
        aluno.setSaldo(50);
        Vantagem vantagem = vantagem(30);
        when(alunoRepository.buscarParaAtualizarPorLogin("ana")).thenReturn(Optional.of(aluno));
        when(vantagemRepository.findById(3L)).thenReturn(Optional.of(vantagem));
        when(resgateRepository.existsByCodigo(any())).thenReturn(false);
        when(resgateRepository.save(any(ResgateVantagem.class))).thenAnswer(i -> i.getArgument(0));

        ResgateVantagem resgate = service.resgatar("ana", 3L);

        assertThat(aluno.getSaldo()).isEqualTo(20);
        assertThat(resgate.getValor()).isEqualTo(30);
        assertThat(resgate.getCodigo()).hasSize(8);
        verify(eventos).publishEvent(any(ResgateRealizado.class));
    }

    @Test
    @DisplayName("Resgate — saldo insuficiente é rejeitado")
    void resgatar_saldoInsuficiente() {
        aluno.setSaldo(10);
        when(alunoRepository.buscarParaAtualizarPorLogin("ana")).thenReturn(Optional.of(aluno));
        when(vantagemRepository.findById(3L)).thenReturn(Optional.of(vantagem(30)));

        assertThatThrownBy(() -> service.resgatar("ana", 3L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(aluno.getSaldo()).isEqualTo(10);
        verify(resgateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resgate — repete a geração quando o código já existe")
    void resgatar_codigoColidindo() {
        aluno.setSaldo(50);
        when(alunoRepository.buscarParaAtualizarPorLogin("ana")).thenReturn(Optional.of(aluno));
        when(vantagemRepository.findById(3L)).thenReturn(Optional.of(vantagem(30)));
        when(resgateRepository.existsByCodigo(any())).thenReturn(true, false);
        when(resgateRepository.save(any(ResgateVantagem.class))).thenAnswer(i -> i.getArgument(0));

        service.resgatar("ana", 3L);

        verify(resgateRepository, times(2)).existsByCodigo(any());
    }

    private Vantagem vantagem(int custo) {
        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome("Cantina");
        empresa.setEmail("contato@cantina.com");
        Vantagem v = new Vantagem();
        v.setEmpresa(empresa);
        v.setNome("Lanche");
        v.setCustoMoedas(custo);
        return v;
    }
}
