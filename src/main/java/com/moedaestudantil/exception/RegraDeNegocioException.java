package com.moedaestudantil.exception;

/**
 * Exceção lançada quando uma regra de negócio é violada
 * (CPF duplicado, CNPJ duplicado, exclusão bloqueada, etc.).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
