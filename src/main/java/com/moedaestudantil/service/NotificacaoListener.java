package com.moedaestudantil.service;

import com.moedaestudantil.event.EnvioMoedasRealizado;
import com.moedaestudantil.event.ResgateRealizado;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispara os e-mails somente após o commit, para que uma falha de SMTP
 * não desfaça a transferência ou o resgate.
 */
@Component
public class NotificacaoListener {

    private final EmailService emailService;

    public NotificacaoListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarMoedas(EnvioMoedasRealizado e) {
        emailService.enviar(e.alunoEmail(),
                "Você recebeu " + e.valor() + " moedas!",
                "Olá, " + e.alunoNome() + "!\n\n"
                        + "O professor " + e.professorNome() + " enviou " + e.valor() + " moedas para você.\n"
                        + "Motivo: " + e.mensagem() + "\n");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoResgatar(ResgateRealizado e) {
        emailService.enviar(e.alunoEmail(),
                "Seu cupom: " + e.vantagemNome(),
                "Olá, " + e.alunoNome() + "!\n\n"
                        + "Você trocou " + e.custo() + " moedas por \"" + e.vantagemNome()
                        + "\" (" + e.empresaNome() + ").\n"
                        + "Apresente o código na empresa: " + e.codigo() + "\n");
        emailService.enviar(e.empresaEmail(),
                "Resgate para conferência: " + e.vantagemNome(),
                "Um aluno resgatou a vantagem \"" + e.vantagemNome() + "\".\n"
                        + "Aluno: " + e.alunoNome() + "\n"
                        + "Código de conferência: " + e.codigo() + "\n");
    }
}
