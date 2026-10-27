package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * ResgateVantagem — especialização de Transacao (JOINED).
 * Gera um código único (mesmo enviado nos dois e-mails).
 */
@Entity
@Table(name = "resgate_vantagem",
        uniqueConstraints = @UniqueConstraint(name = "uq_resgate_codigo", columnNames = "codigo"))
@DiscriminatorValue("RESGATE")
@PrimaryKeyJoinColumn(name = "transacao_id")
public class ResgateVantagem extends Transacao {

    @NotNull(message = "Aluno é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull(message = "Vantagem é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id", nullable = false)
    private Vantagem vantagem;

    @NotBlank(message = "Código é obrigatório")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    public ResgateVantagem() {}

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public Vantagem getVantagem() { return vantagem; }
    public void setVantagem(Vantagem vantagem) { this.vantagem = vantagem; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
