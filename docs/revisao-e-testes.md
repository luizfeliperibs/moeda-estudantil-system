# Revisão de código e bateria de testes — Release 2

Data: 2026-11-10 · Resultado: **55 testes, 0 falhas** (19 anteriores + 36 de integração).

Comando usado (JDK 21 local; o pom pede 24):

```
mvn clean test -Djava.version=21
```

> Se o IDE recompilar `target/` com outro JDK, aparece "class file version 68". Rode `mvn clean` antes.

## 1. Correções feitas na revisão

| Problema | Correção |
|---|---|
| Upload confiava no Content-Type do cliente (HTML enviado como `image/png` era aceito) | `FotoStorageService` confere a assinatura dos primeiros bytes (PNG, JPEG, GIF, WEBP) |
| Assunto do e-mail inclui texto digitado por usuário (header injection) | `EmailService` remove quebras de linha do assunto |
| Foto acima de 2 MB gerava erro 500 no servidor real | `EmpresaPainelController` trata `MaxUploadSizeExceededException` com mensagem amigável |

## 2. Cobertura da bateria

`src/test/java/com/moedaestudantil/integration/FluxosIntegracaoTest.java` (HTTP + Spring Security + JPA/H2 reais) e `service/TransacaoServiceTest.java` (unitário).

| Área | Casos |
|---|---|
| Segurança | anônimo redirecionado ao login; matriz de perfis com 403; POST sem CSRF recusado; login real do seed; cadastro público de aluno/empresa |
| Envio de moedas | válido; motivo vazio; valor 0/negativo/ausente; saldo insuficiente; saldo exato; aluno de outra instituição (inclusive POST forjado); aluno inexistente; mensagem > 1000 caracteres |
| Concorrência | 8 envios simultâneos de 20 com saldo 100 → exatamente 5 passam |
| Extrato | sinais e saldos de professor e aluno; estado vazio |
| Vantagens | upload válido e servido em `/uploads`; sem foto; conteúdo falso; tipo proibido; > 2 MB; validação de campos; edição mantém foto; outra empresa não edita/exclui; exclusão bloqueada após resgate; listagem própria |
| Resgate | mesmo código para aluno e empresa; saldo insuficiente; saldo exato; 30 códigos sem repetição; empresa sem e-mail; falha de e-mail não desfaz o resgate; catálogo com botão desabilitado |
| Outros | crédito semestral (+1.000 acumulável); home por perfil |

**Teste de mutação:** removendo o `@Lock` de `ProfessorRepository` em uma cópia do projeto, o teste de concorrência falha (8 envios passam em vez de 5). Com o lock, passa.

## 3. Pendências conhecidas (não corrigidas)

- `DataSeeder` sem `@Profile("dev")`: criaria professores com senha `prof123` também em produção.
- `/alunos` e `/empresas` (CRUD da Release 1) acessíveis a qualquer usuário autenticado; falta perfil de administrador.
- E-mail da empresa é opcional: sem ele, a empresa não recebe o código de conferência (apenas aviso no log).
- Formulário de edição de vantagem não reexibe a miniatura da foto após erro de validação.
- Fotos substituídas permanecem em `uploads/`.
- Não testado: PostgreSQL (lock pessimista) e execução manual no navegador.
