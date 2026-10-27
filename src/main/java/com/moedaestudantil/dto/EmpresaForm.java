package com.moedaestudantil.dto;

import jakarta.validation.constraints.*;

/**
 * DTO de formulário para cadastro/edição de EmpresaParceira.
 */
public class EmpresaForm {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    private String cnpj;

    @Email(message = "E-mail inválido")
    private String email;

    private String login;

    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
