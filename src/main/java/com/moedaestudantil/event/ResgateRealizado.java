package com.moedaestudantil.event;

/**
 * Publicado após o resgate de uma vantagem; aluno e empresa recebem o mesmo código.
 */
public record ResgateRealizado(String alunoNome, String alunoEmail,
                               String empresaNome, String empresaEmail,
                               String vantagemNome, int custo, String codigo) {}
