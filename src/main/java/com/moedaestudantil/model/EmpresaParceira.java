package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * EmpresaParceira é uma especialização de Usuario (herança JOINED).
 * Contém CNPJ único.
 */
@Entity
@Table(name = "empresa_parceira",
        uniqueConstraints = @UniqueConstraint(name = "uq_empresa_cnpj", columnNames = "cnpj"))
@DiscriminatorValue("EMPRESA")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class EmpresaParceira extends Usuario {

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 200)
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Email(message = "E-mail inválido")
    @Column(length = 150)
    private String email;

    public EmpresaParceira() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
