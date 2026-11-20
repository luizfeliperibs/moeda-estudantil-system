package com.moedaestudantil.event;

/**
 * Publicado após o envio de moedas; o e-mail ao aluno só sai depois do commit.
 */
public record EnvioMoedasRealizado(String alunoNome, String alunoEmail,
                                   String professorNome, int valor, String mensagem) {}
