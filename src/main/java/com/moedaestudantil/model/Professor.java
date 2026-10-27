package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Professor é uma especialização de Usuario (herança JOINED).
 * Pré-cadastrado via seed; sem CRUD de usuário nesta sprint.
 */
@Entity
@Table(name = "professor",
        uniqueConstraints = @UniqueConstraint(name = "uq_professor_cpf", columnNames = "cpf"))
@DiscriminatorValue("PROFESSOR")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Professor extends Usuario {

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(length = 100)
    private String departamento;

    @Min(value = 0, message = "Saldo não pode ser negativo")
    @Column(nullable = false)
    private int saldo = 0;

    @NotNull(message = "Instituição é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    public Professor() {}

    public Professor(String nome, String cpf, String departamento, InstituicaoEnsino instituicao) {
        this.nome = nome;
        this.cpf = cpf;
        this.departamento = departamento;
        this.instituicao = instituicao;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public int getSaldo() { return saldo; }
    public void setSaldo(int saldo) { this.saldo = saldo; }

    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }
}
