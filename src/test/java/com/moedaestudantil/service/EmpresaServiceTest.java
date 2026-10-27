package com.moedaestudantil.service;

import com.moedaestudantil.dto.EmpresaForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.repository.EmpresaParceiraRepository;
import com.moedaestudantil.repository.VantagemRepository;
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
@DisplayName("EmpresaService — testes de serviço")
class EmpresaServiceTest {

    @Mock EmpresaParceiraRepository empresaRepository;
    @Mock VantagemRepository vantagemRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks EmpresaService empresaService;

    private EmpresaForm formValido;

    @BeforeEach
    void setUp() {
        formValido = new EmpresaForm();
        formValido.setNome("Empresa Teste Ltda");
        formValido.setCnpj("12.345.678/0001-99");
        formValido.setEmail("contato@empresa.com");
        formValido.setLogin("empresa.teste");
        formValido.setSenha("senha123");
    }

    @Test
    @DisplayName("Criar empresa — caso feliz")
    void criarEmpresa_casoFeliz() {
        // arrange
        when(usuarioRepository.existsByLogin("empresa.teste")).thenReturn(false);
        when(empresaRepository.existsByCnpj("12.345.678/0001-99")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$hashed");

        EmpresaParceira empresaSalva = new EmpresaParceira();
        empresaSalva.setNome("Empresa Teste Ltda");
        when(empresaRepository.save(any(EmpresaParceira.class))).thenReturn(empresaSalva);

        // act
        EmpresaParceira resultado = empresaService.criar(formValido);

        // assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Empresa Teste Ltda");
        verify(empresaRepository, times(1)).save(any(EmpresaParceira.class));
    }

    @Test
    @DisplayName("Criar empresa — CNPJ duplicado deve lançar exceção")
    void criarEmpresa_cnpjDuplicado() {
        // arrange
        when(usuarioRepository.existsByLogin("empresa.teste")).thenReturn(false);
        when(empresaRepository.existsByCnpj("12.345.678/0001-99")).thenReturn(true); // CNPJ já existe

        // act & assert
        assertThatThrownBy(() -> empresaService.criar(formValido))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CNPJ já cadastrado");

        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Excluir empresa — bloqueado se tiver vantagens resgatadas")
    void excluirEmpresa_comVantagensResgatadas_deveLancarExcecao() {
        // arrange
        EmpresaParceira empresa = new EmpresaParceira();
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(vantagemRepository.empresaTemVantagensResgatadas(1L)).thenReturn(true);

        // act & assert
        assertThatThrownBy(() -> empresaService.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("vantagens que já foram resgatadas");

        verify(empresaRepository, never()).delete(any());
    }
}
