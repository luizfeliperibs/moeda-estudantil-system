# Sistema de Moeda Estudantil — Release 2 (Lab04S01)

Entregas: Diagramas de Sequência, Diagrama de Visão Geral de Interação e implementação dos casos de uso restantes.

> Os diagramas estão em PlantUML (renderize em plantuml.com ou na extensão do VS Code).

## 1. Diagramas

| Diagrama | Arquivo |
|---|---|
| Sequência — Envio de Moedas | `sequencia-envio-moedas.puml` |
| Sequência — Resgate de Vantagem | `sequencia-resgate.puml` |
| Visão Geral de Interação | `visao-geral-interacao.puml` |

## 2. Casos de uso implementados

| Caso de uso | Rota | Ator |
|---|---|---|
| Autenticar e painel por perfil | `/login`, `/home` | Todos |
| Enviar moedas (UC06/UC07) | `GET/POST /professor/enviar` | Professor |
| Extrato do professor (UC08) | `GET /professor/extrato` | Professor |
| Extrato do aluno (UC08) | `GET /aluno/extrato` | Aluno |
| Catálogo de vantagens (UC09) | `GET /aluno/vantagens` | Aluno |
| Resgatar vantagem (UC10/UC11) | `POST /aluno/vantagens/{id}/resgatar` | Aluno |
| Manter vantagens com foto (UC04) | `/empresa/vantagens/**` | Empresa |
| Crédito semestral (UC12) | `@Scheduled` em `SemestreScheduler` | Temporizador |

## 3. Decisões

- **Autorização por perfil:** `/professor/**`, `/aluno/**` e `/empresa/**` exigem a respectiva role. O usuário de cada operação vem sempre do `Principal`, nunca do formulário.
- **Concorrência:** saldos são alterados sob lock pessimista (`SELECT ... FOR UPDATE`), evitando dois envios simultâneos gastarem o mesmo saldo.
- **Atomicidade:** débito, crédito e registro da transação ocorrem em uma única `@Transactional`.
- **E-mail após o commit:** os serviços publicam eventos (`EnvioMoedasRealizado`, `ResgateRealizado`) tratados por `@TransactionalEventListener(AFTER_COMMIT)`; uma falha de SMTP não desfaz a operação. Sem `spring.mail.host`, o e-mail é apenas registrado no log.
- **Instituição:** o professor só envia moedas a alunos da própria instituição.
- **Código do cupom:** 8 caracteres sem ambiguidades (sem 0/O/1/I), gerado com `SecureRandom`, único no banco; o mesmo código segue para aluno e empresa.
- **Foto:** upload validado (JPG/PNG/WEBP/GIF, até 2 MB), salvo em `uploads/` com nome gerado pelo servidor e servido em `/uploads/**`.
- **Professores:** continuam pré-cadastrados (seed), conforme o enunciado; não há cadastro de professor.
