package com.moedaestudantil.dto;

import java.time.LocalDateTime;

/**
 * Linha do extrato. {@code valor} é positivo para entrada e negativo para saída de moedas.
 */
public record ExtratoItem(LocalDateTime dataHora, String tipo, String descricao, int valor) {}
