package com.moedaestudantil.integration;

import com.moedaestudantil.dto.EnvioMoedasForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.*;
import com.moedaestudantil.repository.*;
import com.moedaestudantil.service.EmailService;
import com.moedaestudantil.service.ProfessorService;
import com.moedaestudantil.service.TransacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bateria de integração: HTTP + Spring Security + JPA (H2) reais.
 * Não é @Transactional de propósito: os e-mails saem em AFTER_COMMIT e o lock
 * pessimista só é exercitado com transações reais.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.upload-dir=target/test-uploads")
@DisplayName("Integração — Release 2")
class FluxosIntegracaoTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired MockMvc mvc;
    @Autowired InstituicaoEnsinoRepository instRepo;
    @Autowired ProfessorRepository profRepo;
    @Autowired AlunoRepository alunoRepo;
    @Autowired EmpresaParceiraRepository empresaRepo;
    @Autowired VantagemRepository vantagemRepo;
    @Autowired EnvioMoedasRepository envioRepo;
    @Autowired ResgateVantagemRepository resgateRepo;
    @Autowired TransacaoService transacaoService;
    @Autowired ProfessorService professorService;

    @MockitoBean EmailService emailService;

    InstituicaoEnsino puc;
    InstituicaoEnsino outra;
    Professor prof;
    Aluno aluno;
    Aluno alunoOutraInst;
    EmpresaParceira empresa;

    @BeforeEach
    void dados() {
        String s = UUID.randomUUID().toString().substring(0, 8);
        puc = instRepo.save(new InstituicaoEnsino("Inst-A-" + s));
        outra = instRepo.save(new InstituicaoEnsino("Inst-B-" + s));

        prof = new Professor("Prof " + s, "P" + s, "Depto", puc);
        prof.setLogin("prof." + s);
        prof.setSenhaHash("x");
        prof.setSaldo(100);
        prof = profRepo.save(prof);

        aluno = novoAluno("Aluna " + s, "a." + s, puc, 0);
        alunoOutraInst = novoAluno("Outro " + s, "o." + s, outra, 0);

        empresa = novaEmpresa("Empresa " + s, "e." + s, "contato@" + s + ".com");
    }

    // ---------------------------------------------------------------- helpers

    private Aluno novoAluno(String nome, String login, InstituicaoEnsino inst, int saldo) {
        Aluno a = new Aluno();
        a.setLogin(login);
        a.setSenhaHash("x");
        a.setNome(nome);
        a.setEmail(login + "@aluno.com");
        a.setCpf("A" + login.substring(Math.max(0, login.length() - 12)));
        a.setCurso("ES");
        a.setInstituicao(inst);
        a.setSaldo(saldo);
        return alunoRepo.save(a);
    }

    private EmpresaParceira novaEmpresa(String nome, String login, String email) {
        EmpresaParceira e = new EmpresaParceira();
        e.setLogin(login);
        e.setSenhaHash("x");
        e.setNome(nome);
        e.setCnpj("C" + login.substring(Math.max(0, login.length() - 12)));
        e.setEmail(email);
        return empresaRepo.save(e);
    }

    private Vantagem novaVantagem(EmpresaParceira dona, int custo) {
        Vantagem v = new Vantagem();
        v.setEmpresa(dona);
        v.setNome("Vantagem " + UUID.randomUUID().toString().substring(0, 6));
        v.setDescricao("desc");
        v.setCustoMoedas(custo);
        v.setFotoUrl("/uploads/x.png");
        return vantagemRepo.save(v);
    }

    private RequestPostProcessor comoProfessor() { return user(prof.getLogin()).roles("PROFESSOR"); }
    private RequestPostProcessor comoAluno()     { return user(aluno.getLogin()).roles("ALUNO"); }
    private RequestPostProcessor comoEmpresa()   { return user(empresa.getLogin()).roles("EMPRESA"); }

    private int saldoProf()  { return profRepo.findById(prof.getId()).orElseThrow().getSaldo(); }
    private int saldoAluno() { return alunoRepo.findById(aluno.getId()).orElseThrow().getSaldo(); }

    // ---------------------------------------------------------------- segurança

    @Nested
    @DisplayName("Segurança e autorização por perfil")
    class Seguranca {

        @Test
        @DisplayName("Anônimo é redirecionado ao login nas áreas protegidas")
        void anonimo() throws Exception {
            for (String url : List.of("/professor/enviar", "/professor/extrato", "/aluno/extrato",
                    "/aluno/vantagens", "/empresa/vantagens", "/home")) {
                mvc.perform(get(url)).andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrlPattern("**/login"));
            }
        }

        @Test
        @DisplayName("Cada perfil só acessa a própria área (403 nas demais)")
        void matrizDePerfis() throws Exception {
            mvc.perform(get("/professor/enviar").with(comoAluno())).andExpect(status().isForbidden());
            mvc.perform(get("/professor/extrato").with(comoEmpresa())).andExpect(status().isForbidden());
            mvc.perform(get("/aluno/extrato").with(comoProfessor())).andExpect(status().isForbidden());
            mvc.perform(get("/aluno/vantagens").with(comoEmpresa())).andExpect(status().isForbidden());
            mvc.perform(get("/empresa/vantagens").with(comoAluno())).andExpect(status().isForbidden());
            mvc.perform(get("/empresa/vantagens/novo").with(comoProfessor())).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Aluno não consegue enviar moedas nem cadastrar vantagem via POST")
        void postsProibidos() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoAluno()).with(csrf())
                    .param("alunoId", aluno.getId().toString()).param("valor", "10").param("mensagem", "x"))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/aluno/vantagens/1/resgatar").with(comoProfessor()).with(csrf()))
                    .andExpect(status().isForbidden());
            assertThat(saldoAluno()).isZero();
        }

        @Test
        @DisplayName("POST sem token CSRF é rejeitado")
        void semCsrf() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor())
                    .param("alunoId", aluno.getId().toString()).param("valor", "10").param("mensagem", "x"))
                    .andExpect(status().isForbidden());
            assertThat(saldoProf()).isEqualTo(100);
        }

        @Test
        @DisplayName("Login real com professor do seed redireciona para /home")
        void loginReal() throws Exception {
            mvc.perform(formLogin().user("milena.adao").password("prof123"))
                    .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/home"));
            mvc.perform(formLogin().user("milena.adao").password("errada"))
                    .andExpect(redirectedUrl("/login?erro=true"));
        }

        @Test
        @DisplayName("Cadastro de aluno e empresa continua público")
        void cadastroPublico() throws Exception {
            mvc.perform(get("/alunos/novo")).andExpect(status().isOk());
            mvc.perform(get("/empresas/novo")).andExpect(status().isOk());
        }
    }

    // ---------------------------------------------------------------- envio

    @Nested
    @DisplayName("Envio de moedas via HTTP")
    class Envio {

        @Test
        @DisplayName("Envio válido move saldos, registra transação e notifica o aluno")
        void valido() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", aluno.getId().toString()).param("valor", "40")
                    .param("mensagem", "Excelente seminário"))
                    .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/professor/enviar"));

            assertThat(saldoProf()).isEqualTo(60);
            assertThat(saldoAluno()).isEqualTo(40);
            var envios = envioRepo.findByProfessorIdOrderByDataHoraDesc(prof.getId());
            assertThat(envios).hasSize(1);
            assertThat(envios.get(0).getMensagem()).isEqualTo("Excelente seminário");

            ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
            verify(emailService).enviar(eq(aluno.getEmail()), contains("40"), corpo.capture());
            assertThat(corpo.getValue()).contains("Excelente seminário");
        }

        @Test
        @DisplayName("Motivo vazio: formulário reexibido com erro e nada muda")
        void motivoVazio() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", aluno.getId().toString()).param("valor", "10").param("mensagem", "  "))
                    .andExpect(status().isOk()).andExpect(view().name("professor/enviar"))
                    .andExpect(model().attributeHasFieldErrors("form", "mensagem"));
            assertThat(saldoProf()).isEqualTo(100);
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Valor zero, negativo ou ausente é rejeitado")
        void valoresInvalidos() throws Exception {
            for (String v : new String[]{"0", "-5", ""}) {
                mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                        .param("alunoId", aluno.getId().toString()).param("valor", v).param("mensagem", "ok"))
                        .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "valor"));
            }
            assertThat(saldoProf()).isEqualTo(100);
        }

        @Test
        @DisplayName("Saldo insuficiente: mensagem de erro, saldos e histórico intactos")
        void saldoInsuficiente() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", aluno.getId().toString()).param("valor", "101").param("mensagem", "ok"))
                    .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
            assertThat(saldoProf()).isEqualTo(100);
            assertThat(saldoAluno()).isZero();
            assertThat(envioRepo.findByProfessorId(prof.getId())).isEmpty();
        }

        @Test
        @DisplayName("Enviar exatamente todo o saldo é permitido e zera o professor")
        void saldoExato() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", aluno.getId().toString()).param("valor", "100").param("mensagem", "tudo"))
                    .andExpect(status().is3xxRedirection());
            assertThat(saldoProf()).isZero();
            assertThat(saldoAluno()).isEqualTo(100);
        }

        @Test
        @DisplayName("Lista de alunos mostra só a própria instituição e POST forjado para outra é rejeitado")
        void escopoDeInstituicao() throws Exception {
            mvc.perform(get("/professor/enviar").with(comoProfessor()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(aluno.getNome())))
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString(alunoOutraInst.getNome()))));

            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", alunoOutraInst.getId().toString()).param("valor", "10").param("mensagem", "x"))
                    .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
            assertThat(saldoProf()).isEqualTo(100);
        }

        @Test
        @DisplayName("Aluno inexistente é tratado como erro de negócio, não 500")
        void alunoInexistente() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", "99999999").param("valor", "10").param("mensagem", "x"))
                    .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        }

        @Test
        @DisplayName("Mensagem acima de 1000 caracteres é rejeitada")
        void mensagemGrande() throws Exception {
            mvc.perform(post("/professor/enviar").with(comoProfessor()).with(csrf())
                    .param("alunoId", aluno.getId().toString()).param("valor", "10")
                    .param("mensagem", "x".repeat(1001)))
                    .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "mensagem"));
        }
    }

    // ---------------------------------------------------------------- concorrência

    @Test
    @DisplayName("Concorrência: 8 envios simultâneos de 20 com saldo 100 → exatamente 5 passam")
    void concorrencia() throws Exception {
        int tentativas = 8;
        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger recusados = new AtomicInteger();
        List<Future<?>> futuros = new ArrayList<>();
        for (int i = 0; i < tentativas; i++) {
            futuros.add(pool.submit(() -> {
                largada.await();
                EnvioMoedasForm f = new EnvioMoedasForm();
                f.setAlunoId(aluno.getId());
                f.setValor(20);
                f.setMensagem("concorrente");
                try {
                    transacaoService.enviarMoedas(prof.getLogin(), f);
                    ok.incrementAndGet();
                } catch (RegraDeNegocioException e) {
                    recusados.incrementAndGet();
                }
                return null;
            }));
        }
        largada.countDown();
        for (Future<?> f : futuros) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(ok.get()).isEqualTo(5);
        assertThat(recusados.get()).isEqualTo(3);
        assertThat(saldoProf()).isZero();
        assertThat(saldoAluno()).isEqualTo(100);
        assertThat(envioRepo.findByProfessorId(prof.getId())).hasSize(5);
    }

    // ---------------------------------------------------------------- extrato

    @Test
    @DisplayName("Extratos refletem envio e resgate, com sinais e saldo corretos")
    void extratos() throws Exception {
        transacaoService.enviarMoedas(prof.getLogin(), form(aluno, 50, "Participação ativa"));
        Vantagem v = novaVantagem(empresa, 30);
        transacaoService.resgatar(aluno.getLogin(), v.getId());

        mvc.perform(get("/professor/extrato").with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Participação ativa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("-50")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(aluno.getNome())));

        String html = mvc.perform(get("/aluno/extrato").with(comoAluno()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Participação ativa", "+50", "-30", v.getNome(), ">20<");
    }

    @Test
    @DisplayName("Extrato de quem não tem movimentação exibe estado vazio")
    void extratoVazio() throws Exception {
        mvc.perform(get("/aluno/extrato").with(comoAluno()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nenhuma movimentação")));
        mvc.perform(get("/professor/extrato").with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ainda não enviou")));
    }

    // ---------------------------------------------------------------- vantagens

    @Nested
    @DisplayName("Vantagens da empresa (upload de foto)")
    class Vantagens {

        private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder criar() {
            return multipart("/empresa/vantagens");
        }

        @Test
        @DisplayName("Cadastro com foto PNG válida salva arquivo e é servido em /uploads")
        void cadastroComFoto() throws Exception {
            mvc.perform(criar().file(new MockMultipartFile("foto", "f.png", "image/png", PNG))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "Cupom 10%").param("descricao", "Desconto").param("custoMoedas", "25"))
                    .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/empresa/vantagens"));

            Vantagem v = vantagemRepo.findByEmpresaIdOrderByNomeAsc(empresa.getId()).get(0);
            assertThat(v.getFotoUrl()).startsWith("/uploads/").endsWith(".png");
            assertThat(v.getCustoMoedas()).isEqualTo(25);

            mvc.perform(get(v.getFotoUrl()).with(comoAluno()))
                    .andExpect(status().isOk()).andExpect(content().bytes(PNG));
            mvc.perform(get(v.getFotoUrl())).andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("Sem foto no cadastro: erro e nada é criado")
        void semFoto() throws Exception {
            mvc.perform(criar().file(new MockMultipartFile("foto", "", "application/octet-stream", new byte[0]))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "X").param("descricao", "d").param("custoMoedas", "5"))
                    .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
            assertThat(vantagemRepo.findByEmpresaId(empresa.getId())).isEmpty();
        }

        @Test
        @DisplayName("Arquivo que não é imagem (HTML com content-type de imagem) é recusado")
        void conteudoFalso() throws Exception {
            mvc.perform(criar().file(new MockMultipartFile("foto", "x.png", "image/png",
                            "<script>alert(1)</script>".getBytes()))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "X").param("descricao", "d").param("custoMoedas", "5"))
                    .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
            assertThat(vantagemRepo.findByEmpresaId(empresa.getId())).isEmpty();
        }

        @Test
        @DisplayName("Tipo não permitido (PDF/SVG) e arquivo acima de 2 MB são recusados")
        void tipoETamanho() throws Exception {
            mvc.perform(criar().file(new MockMultipartFile("foto", "x.svg", "image/svg+xml", "<svg/>".getBytes()))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "X").param("descricao", "d").param("custoMoedas", "5"))
                    .andExpect(model().attributeExists("erro"));

            byte[] grande = new byte[2 * 1024 * 1024 + 1];
            System.arraycopy(PNG, 0, grande, 0, PNG.length);
            mvc.perform(criar().file(new MockMultipartFile("foto", "g.png", "image/png", grande))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "X").param("descricao", "d").param("custoMoedas", "5"))
                    .andExpect(model().attributeExists("erro"));
            assertThat(vantagemRepo.findByEmpresaId(empresa.getId())).isEmpty();
        }

        @Test
        @DisplayName("Campos obrigatórios e custo > 0 são validados")
        void validacao() throws Exception {
            mvc.perform(criar().file(new MockMultipartFile("foto", "f.png", "image/png", PNG))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "").param("descricao", "").param("custoMoedas", "0"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("form", "nome", "descricao", "custoMoedas"));
            assertThat(vantagemRepo.findByEmpresaId(empresa.getId())).isEmpty();
        }

        @Test
        @DisplayName("Edição sem nova foto mantém a foto anterior")
        void editarMantemFoto() throws Exception {
            Vantagem v = novaVantagem(empresa, 10);
            mvc.perform(multipart("/empresa/vantagens/{id}", v.getId())
                    .file(new MockMultipartFile("foto", "", "application/octet-stream", new byte[0]))
                    .with(comoEmpresa()).with(csrf())
                    .param("nome", "Novo nome").param("descricao", "nova").param("custoMoedas", "15"))
                    .andExpect(status().is3xxRedirection());
            Vantagem depois = vantagemRepo.findById(v.getId()).orElseThrow();
            assertThat(depois.getNome()).isEqualTo("Novo nome");
            assertThat(depois.getCustoMoedas()).isEqualTo(15);
            assertThat(depois.getFotoUrl()).isEqualTo("/uploads/x.png");
        }

        @Test
        @DisplayName("Empresa não edita nem exclui vantagem de outra empresa")
        void propriedade() throws Exception {
            Vantagem alheia = novaVantagem(empresa, 10);
            EmpresaParceira intrusa = novaEmpresa("Intrusa", "i." + UUID.randomUUID().toString().substring(0, 8), "i@i.com");
            RequestPostProcessor comoIntrusa = user(intrusa.getLogin()).roles("EMPRESA");

            mvc.perform(get("/empresa/vantagens/{id}/editar", alheia.getId()).with(comoIntrusa))
                    .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("erro"));
            mvc.perform(multipart("/empresa/vantagens/{id}", alheia.getId()).with(comoIntrusa).with(csrf())
                    .param("nome", "Hackeada").param("descricao", "d").param("custoMoedas", "1"))
                    .andExpect(model().attributeExists("erro"));
            mvc.perform(post("/empresa/vantagens/{id}/excluir", alheia.getId()).with(comoIntrusa).with(csrf()))
                    .andExpect(flash().attributeExists("erro"));

            Vantagem depois = vantagemRepo.findById(alheia.getId()).orElseThrow();
            assertThat(depois.getNome()).isEqualTo(alheia.getNome());
        }

        @Test
        @DisplayName("Exclusão sem resgates funciona; com resgate é bloqueada")
        void exclusao() throws Exception {
            Vantagem livre = novaVantagem(empresa, 10);
            mvc.perform(post("/empresa/vantagens/{id}/excluir", livre.getId()).with(comoEmpresa()).with(csrf()))
                    .andExpect(flash().attributeExists("sucesso"));
            assertThat(vantagemRepo.existsById(livre.getId())).isFalse();

            Vantagem usada = novaVantagem(empresa, 10);
            aluno.setSaldo(50);
            alunoRepo.save(aluno);
            transacaoService.resgatar(aluno.getLogin(), usada.getId());
            mvc.perform(post("/empresa/vantagens/{id}/excluir", usada.getId()).with(comoEmpresa()).with(csrf()))
                    .andExpect(flash().attributeExists("erro"));
            assertThat(vantagemRepo.existsById(usada.getId())).isTrue();
        }

        @Test
        @DisplayName("Listagem da empresa mostra só as próprias vantagens")
        void listagemPropria() throws Exception {
            Vantagem minha = novaVantagem(empresa, 10);
            EmpresaParceira outraEmp = novaEmpresa("Outra", "x." + UUID.randomUUID().toString().substring(0, 8), "x@x.com");
            Vantagem dela = novaVantagem(outraEmp, 10);
            mvc.perform(get("/empresa/vantagens").with(comoEmpresa()))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(minha.getNome())))
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString(dela.getNome()))));
        }
    }

    // ---------------------------------------------------------------- resgate

    @Nested
    @DisplayName("Resgate de vantagem via HTTP")
    class Resgate {

        @Test
        @DisplayName("Resgate válido debita, gera código e envia o MESMO código ao aluno e à empresa")
        void valido() throws Exception {
            aluno.setSaldo(50);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 30);

            mvc.perform(post("/aluno/vantagens/{id}/resgatar", v.getId()).with(comoAluno()).with(csrf()))
                    .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/aluno/vantagens"))
                    .andExpect(flash().attributeExists("sucesso"));

            assertThat(saldoAluno()).isEqualTo(20);
            var resgates = resgateRepo.findByAlunoId(aluno.getId());
            assertThat(resgates).hasSize(1);
            String codigo = resgates.get(0).getCodigo();
            assertThat(codigo).matches("[A-Z2-9]{8}");

            ArgumentCaptor<String> paraAluno = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> paraEmpresa = ArgumentCaptor.forClass(String.class);
            verify(emailService).enviar(eq(aluno.getEmail()), anyString(), paraAluno.capture());
            verify(emailService).enviar(eq(empresa.getEmail()), anyString(), paraEmpresa.capture());
            assertThat(paraAluno.getValue()).contains(codigo);
            assertThat(paraEmpresa.getValue()).contains(codigo);
        }

        @Test
        @DisplayName("Saldo insuficiente: erro, nada debitado, nenhum e-mail")
        void saldoInsuficiente() throws Exception {
            aluno.setSaldo(10);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 30);

            mvc.perform(post("/aluno/vantagens/{id}/resgatar", v.getId()).with(comoAluno()).with(csrf()))
                    .andExpect(flash().attributeExists("erro"));
            assertThat(saldoAluno()).isEqualTo(10);
            assertThat(resgateRepo.findByAlunoId(aluno.getId())).isEmpty();
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Saldo exatamente igual ao custo permite o resgate e zera o aluno")
        void saldoExato() throws Exception {
            aluno.setSaldo(30);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 30);
            mvc.perform(post("/aluno/vantagens/{id}/resgatar", v.getId()).with(comoAluno()).with(csrf()))
                    .andExpect(flash().attributeExists("sucesso"));
            assertThat(saldoAluno()).isZero();
        }

        @Test
        @DisplayName("Vantagem inexistente vira mensagem de erro, não 500")
        void inexistente() throws Exception {
            mvc.perform(post("/aluno/vantagens/{id}/resgatar", 99999999L).with(comoAluno()).with(csrf()))
                    .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("erro"));
        }

        @Test
        @DisplayName("Códigos de resgates distintos são diferentes")
        void codigosUnicos() throws Exception {
            aluno.setSaldo(1000);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 1);
            for (int i = 0; i < 30; i++) transacaoService.resgatar(aluno.getLogin(), v.getId());
            var codigos = resgateRepo.findByAlunoId(aluno.getId()).stream().map(ResgateVantagem::getCodigo).toList();
            assertThat(codigos).hasSize(30).doesNotHaveDuplicates();
            assertThat(saldoAluno()).isEqualTo(970);
        }

        @Test
        @DisplayName("Empresa sem e-mail: resgate ocorre e só o aluno é notificado")
        void empresaSemEmail() throws Exception {
            empresa.setEmail(null);
            empresaRepo.save(empresa);
            aluno.setSaldo(50);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 10);
            transacaoService.resgatar(aluno.getLogin(), v.getId());
            verify(emailService).enviar(eq(aluno.getEmail()), anyString(), anyString());
            verify(emailService).enviar(isNull(), anyString(), anyString());
        }

        @Test
        @DisplayName("Catálogo exibe foto/custo e desabilita o botão quando o saldo não basta")
        void catalogo() throws Exception {
            aluno.setSaldo(5);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 30);
            String html = mvc.perform(get("/aluno/vantagens").with(comoAluno()))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(html).contains(v.getNome(), "30 moedas", "/uploads/x.png", "disabled");
        }

        @Test
        @DisplayName("Falha de e-mail não desfaz o resgate")
        void falhaDeEmail() throws Exception {
            doThrow(new RuntimeException("smtp fora")).when(emailService).enviar(any(), any(), any());
            aluno.setSaldo(50);
            alunoRepo.save(aluno);
            Vantagem v = novaVantagem(empresa, 10);
            try {
                transacaoService.resgatar(aluno.getLogin(), v.getId());
            } catch (RuntimeException ignorada) {
                // exceção do listener pós-commit; o estado já foi persistido
            }
            assertThat(saldoAluno()).isEqualTo(40);
            assertThat(resgateRepo.findByAlunoId(aluno.getId())).hasSize(1);
        }
    }

    // ---------------------------------------------------------------- semestre / home

    @Test
    @DisplayName("Crédito semestral soma 1.000 ao saldo existente (acumulável)")
    void creditoSemestral() {
        int antes = saldoProf();
        professorService.creditarSemestre();
        assertThat(saldoProf()).isEqualTo(antes + 1000);
    }

    @Test
    @DisplayName("Home mostra apenas os cartões do perfil logado")
    void homePorPerfil() throws Exception {
        mvc.perform(get("/home").with(comoProfessor()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Enviar moedas")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/aluno/vantagens"))));
        mvc.perform(get("/home").with(comoAluno()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/aluno/vantagens")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/professor/enviar"))));
        mvc.perform(get("/home").with(comoEmpresa()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/empresa/vantagens")));
    }

    private EnvioMoedasForm form(Aluno a, int valor, String msg) {
        EnvioMoedasForm f = new EnvioMoedasForm();
        f.setAlunoId(a.getId());
        f.setValor(valor);
        f.setMensagem(msg);
        return f;
    }
}
