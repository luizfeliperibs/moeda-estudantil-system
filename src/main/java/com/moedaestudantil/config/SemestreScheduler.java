package com.moedaestudantil.config;

import com.moedaestudantil.service.ProfessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Credita 1.000 moedas a cada professor no início de cada semestre (1º de janeiro e 1º de julho).
 */
@Configuration
@EnableScheduling
public class SemestreScheduler {

    private static final Logger log = LoggerFactory.getLogger(SemestreScheduler.class);

    private final ProfessorService professorService;

    public SemestreScheduler(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @Scheduled(cron = "0 0 0 1 1,7 *")
    public void creditarSemestre() {
        int total = professorService.creditarSemestre();
        log.info("[Semestre] {} professores receberam {} moedas.", total, ProfessorService.CREDITO_SEMESTRAL);
    }
}
