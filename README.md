# Sistema de Moeda Estudantil

Sistema web para estimular o reconhecimento do mérito estudantil por meio de uma moeda virtual. Professores distribuem moedas aos alunos como forma de reconhecimento, e os alunos as trocam por produtos e descontos oferecidos por empresas parceiras.

Projeto do **Laboratório 3** da disciplina **Projeto de Software**, do curso de Engenharia de Software da **PUC Minas** (2º semestre de 2026), sob orientação da **Profa. Milena Menezes Adão**.

---

## Integrantes

| Nome |
|---|
| Henrique Pereira |
| Gabriel Santos |
| Luiz Henrique Horta |
| Luiz Felipe Ribeiro |

---

## Sobre o sistema

### Perfis de usuário

| Perfil | O que faz |
|---|---|
| **Aluno** | Se cadastra, recebe moedas, consulta o extrato e troca moedas por vantagens |
| **Professor** | Pré-cadastrado pela instituição; envia moedas aos alunos com uma mensagem de reconhecimento e consulta o extrato |
| **Empresa parceira** | Se cadastra e oferece vantagens (descrição, foto e custo em moedas); recebe e-mail para conferir as trocas |

### Regras de negócio principais

- Alunos informam nome, e-mail, CPF, RG, endereço, instituição de ensino e curso no cadastro.
- Instituições e professores são **pré-cadastrados**; cada professor tem nome, CPF, departamento e vínculo explícito com uma instituição.
- A cada semestre, cada professor recebe **1.000 moedas**, acumuláveis com o saldo atual.
- Para enviar moedas, o professor precisa de saldo suficiente e deve informar uma **mensagem obrigatória** com o motivo.
- O aluno é **notificado por e-mail** ao receber moedas.
- Professores e alunos consultam o **extrato** (saldo e transações).
- Ao trocar moedas por uma vantagem, o valor é debitado do saldo e um **e-mail com cupom** é enviado ao aluno e outro ao parceiro, ambos com o **mesmo código** gerado pelo sistema.
- Todos os perfis acessam o sistema com **login e senha**, e todas as funcionalidades exigem autenticação.

---

## Tecnologias

> A stack abaixo é a proposta inicial do grupo e será confirmada na Sprint 2.

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot |
| Arquitetura | MVC |
| Persistência | Spring Data JPA (ORM) |
| Banco de dados | A definir (ex.: PostgreSQL; H2 para desenvolvimento) |
| Front-end | A definir (ex.: Thymeleaf, renderizado no servidor) |
| Segurança | Spring Security (login, senha com hash e controle por perfil) |
| E-mail | Spring Mail (SMTP) |
| Build | Maven |
| Versionamento | Git e GitHub |

---

## Arquitetura

O sistema segue o padrão **MVC**:

- **Model**: entidades de domínio (`Aluno`, `Professor`, `EmpresaParceira`, `InstituicaoEnsino`, `Vantagem`, `Transacao`, `EnvioMoedas`, `ResgateVantagem`).
- **View**: páginas de cadastro, login, extrato e catálogo de vantagens.
- **Controller**: recebem as requisições, validam a entrada e delegam às regras de negócio.

Apoiam essas camadas os **serviços** (regras de negócio), os **repositórios/DAO** (acesso ao banco), o serviço de **e-mail** e o **agendador** que credita as moedas semestrais.

Os diagramas completos estão em [`docs/`](docs/).

---

## Documentação e modelagem

| Artefato | Local | Sprint |
|---|---|---|
| Casos de uso, histórias de usuário, classes e componentes | [`docs/sprint1.md`](docs/sprint1.md) | 1 |
| Diagrama de casos de uso (PlantUML) | [`docs/casos-de-uso.puml`](docs/casos-de-uso.puml) | 1 |
| Modelo ER | `docs/` (a produzir) | 2 |
| Slides da arquitetura e da persistência | `docs/` (a produzir) | 3 |

Os modelos UML são versionados neste repositório e atualizados sempre que o projeto exigir.

---

## Cronograma

- [x] **Sprint 1 (Lab03S01)**: diagrama de casos de uso, histórias do usuário, diagrama de classes e diagrama de componentes
- [ ] **Sprint 2 (Lab03S02)**: modelo ER, estratégia de acesso ao banco (ORM, DAO etc.) e CRUDs de aluno e empresa parceira (versão inicial: front-end e comunicação com o back-end)
- [ ] **Sprint 3 (Lab03S03)**: CRUDs de aluno e empresa parceira (versão final), apresentação da arquitetura e da camada de persistência, protótipo final e tutorial das tecnologias escolhidas

---

## Estrutura do repositório

```
.
├── docs/                # modelos UML, modelo ER e slides
├── src/
│   ├── main/java/       # código-fonte (model, view/controller, service, repository)
│   └── main/resources/  # configurações e templates
├── pom.xml
└── README.md
```

---

## Como executar

> Esta seção será preenchida a partir da Sprint 2, quando houver código executável.

Pré-requisitos previstos:

- JDK 25
- Maven
- Banco de dados configurado (ver `application.properties`)

```bash
git clone <url-do-repositorio>
cd <pasta-do-repositorio>
mvn spring-boot:run
```

---

## Convenções do grupo

- Branch principal: `main`. O trabalho é feito em branches por funcionalidade e integrado por pull request.
- Mensagens de commit curtas e no imperativo (ex.: `Adiciona cadastro de aluno`).
- Modelos UML sempre atualizados junto com o código.
