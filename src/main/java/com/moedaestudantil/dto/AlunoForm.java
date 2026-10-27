package com.moedaestudantil.dto;

import jakarta.validation.constraints.*;

/**
 * DTO de formulário para cadastro/edição de Aluno.
 * Não expõe entidade nem senha nos templates.
 */
public class AlunoForm {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    private String rg;

    private String endereco;

    @NotBlank(message = "Curso é obrigatório")
    private String curso;

    @NotNull(message = "Instituição é obrigatória")
    private Long instituicaoId;

    // Apenas para cadastro (novo aluno); em edição esses campos ficam em branco
    private String login;

    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public Long getInstituicaoId() { return instituicaoId; }
    public void setInstituicaoId(Long instituicaoId) { this.instituicaoId = instituicaoId; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
