package com.moedaestudantil.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mails. Sem {@code spring.mail.host} configurado (dev), a mensagem
 * é apenas registrada no log. Falhas de envio nunca propagam para o chamador.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${app.mail.from}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviar(String para, String assunto, String corpo) {
        if (para == null || para.isBlank()) {
            log.warn("[E-mail] destinatário ausente; mensagem '{}' não enviada.", assunto);
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[E-mail simulado] Para: {} | Assunto: {}\n{}", para, assunto, corpo);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(para);
            // o assunto pode conter texto digitado por usuários: sem quebras de linha (header injection)
            msg.setSubject(assunto.replaceAll("[\\r\\n]+", " "));
            msg.setText(corpo);
            sender.send(msg);
        } catch (MailException e) {
            log.error("[E-mail] falha ao enviar '{}' para {}: {}", assunto, para, e.getMessage());
        }
    }
}
