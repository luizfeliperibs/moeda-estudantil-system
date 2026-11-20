package com.moedaestudantil.dto;

import jakarta.validation.constraints.*;

/**
 * DTO do formulário de envio de moedas (professor → aluno).
 */
public class EnvioMoedasForm {

    @NotNull(message = "Selecione um aluno")
    private Long alunoId;

    @NotNull(message = "Informe a quantidade de moedas")
    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    private Integer valor;

    @NotBlank(message = "O motivo do envio é obrigatório")
    @Size(max = 1000, message = "O motivo deve ter no máximo 1000 caracteres")
    private String mensagem;

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public Integer getValor() { return valor; }
    public void setValor(Integer valor) { this.valor = valor; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
