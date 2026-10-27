package com.moedaestudantil.config;

import com.moedaestudantil.model.InstituicaoEnsino;
import com.moedaestudantil.model.Professor;
import com.moedaestudantil.repository.InstituicaoEnsinoRepository;
import com.moedaestudantil.repository.ProfessorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed de dados para o perfil de desenvolvimento.
 * Cria 2 instituições e alguns professores pré-cadastrados conforme o enunciado.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(InstituicaoEnsinoRepository instRepo,
                               ProfessorRepository profRepo,
                               PasswordEncoder encoder) {
        return args -> {
            if (instRepo.count() > 0) {
                return; // já foi populado
            }

            // 2 Instituições de ensino
            InstituicaoEnsino pucMinas = instRepo.save(new InstituicaoEnsino("PUC Minas"));
            InstituicaoEnsino ufmg = instRepo.save(new InstituicaoEnsino("UFMG"));

            // Professores pré-cadastrados — senha padrão "prof123"
            String senhaHash = encoder.encode("prof123");

            Professor p1 = new Professor("Milena Menezes Adão", "111.111.111-11", "Engenharia de Software", pucMinas);
            p1.setLogin("milena.adao");
            p1.setSenhaHash(senhaHash);
            p1.setSaldo(1000);
            profRepo.save(p1);

            Professor p2 = new Professor("João Silva", "222.222.222-22", "Ciência da Computação", pucMinas);
            p2.setLogin("joao.silva");
            p2.setSenhaHash(senhaHash);
            p2.setSaldo(1000);
            profRepo.save(p2);

            Professor p3 = new Professor("Ana Costa", "333.333.333-33", "Sistemas de Informação", ufmg);
            p3.setLogin("ana.costa");
            p3.setSenhaHash(senhaHash);
            p3.setSaldo(1000);
            profRepo.save(p3);

            System.out.println("[Seed] Instituições e professores criados com sucesso.");
            System.out.println("[Seed] Login dos professores: milena.adao, joao.silva, ana.costa | Senha: prof123");
        };
    }
}
