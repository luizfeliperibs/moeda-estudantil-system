package com.moedaestudantil.controller;

import com.moedaestudantil.dto.EnvioMoedasForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.service.ExtratoService;
import com.moedaestudantil.service.ProfessorService;
import com.moedaestudantil.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Área do professor: envio de moedas e extrato.
 * O professor é sempre o usuário autenticado, nunca um id vindo do formulário.
 */
@Controller
@RequestMapping("/professor")
public class ProfessorController {

    private final ProfessorService professorService;
    private final TransacaoService transacaoService;
    private final ExtratoService extratoService;

    public ProfessorController(ProfessorService professorService,
                               TransacaoService transacaoService,
                               ExtratoService extratoService) {
        this.professorService = professorService;
        this.transacaoService = transacaoService;
        this.extratoService = extratoService;
    }

    // GET /professor/enviar — formulário de envio
    @GetMapping("/enviar")
    public String enviarForm(Model model, Principal principal) {
        model.addAttribute("form", new EnvioMoedasForm());
        carregarTela(model, principal);
        return "professor/enviar";
    }

    // POST /professor/enviar — envia moedas
    @PostMapping("/enviar")
    public String enviar(@Valid @ModelAttribute("form") EnvioMoedasForm form,
                         BindingResult result,
                         Model model,
                         Principal principal,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                transacaoService.enviarMoedas(principal.getName(), form);
                redirect.addFlashAttribute("sucesso", "Moedas enviadas com sucesso!");
                return "redirect:/professor/enviar";
            } catch (RegraDeNegocioException e) {
                model.addAttribute("erro", e.getMessage());
            }
        }
        carregarTela(model, principal);
        return "professor/enviar";
    }

    // GET /professor/extrato — saldo e envios realizados
    @GetMapping("/extrato")
    public String extrato(Model model, Principal principal) {
        model.addAttribute("professor", professorService.buscarPorLogin(principal.getName()));
        model.addAttribute("itens", extratoService.extratoProfessor(principal.getName()));
        return "professor/extrato";
    }

    private void carregarTela(Model model, Principal principal) {
        model.addAttribute("professor", professorService.buscarPorLogin(principal.getName()));
        model.addAttribute("alunos", professorService.listarAlunosElegiveis(principal.getName()));
    }
}
