package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * EnvioMoedas — especialização de Transacao (JOINED).
 * Registra professor→aluno com mensagem obrigatória.
 */
@Entity
@Table(name = "envio_moedas")
@DiscriminatorValue("ENVIO")
@PrimaryKeyJoinColumn(name = "transacao_id")
public class EnvioMoedas extends Transacao {

    @NotNull(message = "Professor é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @NotNull(message = "Aluno é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotBlank(message = "Mensagem é obrigatória")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    public EnvioMoedas() {}

    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
