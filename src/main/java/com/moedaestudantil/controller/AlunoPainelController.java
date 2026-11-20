package com.moedaestudantil.controller;

import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.ResgateVantagem;
import com.moedaestudantil.service.AlunoService;
import com.moedaestudantil.service.ExtratoService;
import com.moedaestudantil.service.TransacaoService;
import com.moedaestudantil.service.VantagemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Área do aluno: extrato, catálogo de vantagens e resgate.
 */
@Controller
@RequestMapping("/aluno")
public class AlunoPainelController {

    private final AlunoService alunoService;
    private final ExtratoService extratoService;
    private final VantagemService vantagemService;
    private final TransacaoService transacaoService;

    public AlunoPainelController(AlunoService alunoService,
                                 ExtratoService extratoService,
                                 VantagemService vantagemService,
                                 TransacaoService transacaoService) {
        this.alunoService = alunoService;
        this.extratoService = extratoService;
        this.vantagemService = vantagemService;
        this.transacaoService = transacaoService;
    }

    // GET /aluno/extrato — saldo e histórico
    @GetMapping("/extrato")
    public String extrato(Model model, Principal principal) {
        model.addAttribute("aluno", alunoService.buscarPorLogin(principal.getName()));
        model.addAttribute("itens", extratoService.extratoAluno(principal.getName()));
        return "aluno/extrato";
    }

    // GET /aluno/vantagens — catálogo
    @GetMapping("/vantagens")
    public String vantagens(Model model, Principal principal) {
        model.addAttribute("aluno", alunoService.buscarPorLogin(principal.getName()));
        model.addAttribute("vantagens", vantagemService.listarCatalogo());
        return "aluno/vantagens";
    }

    // POST /aluno/vantagens/{id}/resgatar — troca moedas por vantagem
    @PostMapping("/vantagens/{id}/resgatar")
    public String resgatar(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            ResgateVantagem resgate = transacaoService.resgatar(principal.getName(), id);
            redirect.addFlashAttribute("sucesso",
                    "Resgate realizado! Seu código é " + resgate.getCodigo()
                            + " (também enviado por e-mail).");
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/aluno/vantagens";
    }
}
