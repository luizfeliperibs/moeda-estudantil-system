package com.moedaestudantil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Transação abstrata (herança JOINED) — base para EnvioMoedas e ResgateVantagem.
 */
@Entity
@Table(name = "transacao")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Min(value = 1, message = "Valor da transação deve ser maior que zero")
    @Column(nullable = false)
    private int valor;

    @Column(name = "tipo", insertable = false, updatable = false, length = 20)
    private String tipo;

    public Transacao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    public String getTipo() { return tipo; }
}
