package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Entidade InstituicaoEnsino conforme o modelo ER.
 * Pré-cadastrada via seed; não tem CRUD no front-end nesta sprint.
 */
@Entity
@Table(name = "instituicao_ensino",
        uniqueConstraints = @UniqueConstraint(name = "uq_instituicao_nome", columnNames = "nome"))
public class InstituicaoEnsino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da instituição é obrigatório")
    @Column(nullable = false, unique = true, length = 200)
    private String nome;

    public InstituicaoEnsino() {}

    public InstituicaoEnsino(String nome) {
        this.nome = nome;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String toString() {
        return nome;
    }
}
