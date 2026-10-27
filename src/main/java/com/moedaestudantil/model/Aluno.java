package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Aluno é uma especialização de Usuario (herança JOINED).
 * Contém CPF, e-mail (únicos), RG, endereço, curso, saldo e vínculo com instituição.
 */
@Entity
@Table(name = "aluno",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_aluno_cpf",   columnNames = "cpf"),
                @UniqueConstraint(name = "uq_aluno_email", columnNames = "email")
        })
@DiscriminatorValue("ALUNO")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Aluno extends Usuario {

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(length = 20)
    private String rg;

    @Column(length = 255)
    private String endereco;

    @NotBlank(message = "Curso é obrigatório")
    @Column(nullable = false, length = 100)
    private String curso;

    @Min(value = 0, message = "Saldo não pode ser negativo")
    @Column(nullable = false)
    private int saldo = 0;

    @NotNull(message = "Instituição é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    public Aluno() {}

    // Getters e Setters
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

    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) { this.saldo = saldo; }

    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }
}
