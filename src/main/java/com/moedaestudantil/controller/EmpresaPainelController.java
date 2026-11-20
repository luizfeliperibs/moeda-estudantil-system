package com.moedaestudantil.controller;

import com.moedaestudantil.dto.VantagemForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.Vantagem;
import com.moedaestudantil.service.VantagemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Área da empresa parceira: gerenciamento das próprias vantagens.
 */
@Controller
@RequestMapping("/empresa/vantagens")
public class EmpresaPainelController {

    private final VantagemService vantagemService;

    public EmpresaPainelController(VantagemService vantagemService) {
        this.vantagemService = vantagemService;
    }

    // Arquivo acima do limite do multipart (2 MB): volta à listagem com mensagem amigável
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String arquivoGrande(RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", "A foto deve ter no máximo 2 MB.");
        return "redirect:/empresa/vantagens";
    }

    // GET /empresa/vantagens — listar
    @GetMapping
    public String listar(Model model, Principal principal) {
        model.addAttribute("vantagens", vantagemService.listarDaEmpresa(principal.getName()));
        return "empresa/vantagens";
    }

    // GET /empresa/vantagens/novo — formulário de cadastro
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("form", new VantagemForm());
        return "empresa/vantagem-form";
    }

    // POST /empresa/vantagens — criar
    @PostMapping
    public String criar(@Valid @ModelAttribute("form") VantagemForm form,
                        BindingResult result,
                        Model model,
                        Principal principal,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "empresa/vantagem-form";
        }
        try {
            vantagemService.criar(principal.getName(), form);
            redirect.addFlashAttribute("sucesso", "Vantagem cadastrada com sucesso!");
            return "redirect:/empresa/vantagens";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "empresa/vantagem-form";
        }
    }

    // GET /empresa/vantagens/{id}/editar — formulário de edição
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model, Principal principal,
                             RedirectAttributes redirect) {
        try {
            Vantagem v = vantagemService.buscarDaEmpresa(principal.getName(), id);
            VantagemForm form = new VantagemForm();
            form.setId(v.getId());
            form.setNome(v.getNome());
            form.setDescricao(v.getDescricao());
            form.setCustoMoedas(v.getCustoMoedas());
            form.setFotoUrl(v.getFotoUrl());
            model.addAttribute("form", form);
            return "empresa/vantagem-form";
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/empresa/vantagens";
        }
    }

    // POST /empresa/vantagens/{id} — atualizar
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") VantagemForm form,
                            BindingResult result,
                            Model model,
                            Principal principal,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "empresa/vantagem-form";
        }
        try {
            vantagemService.atualizar(principal.getName(), id, form);
            redirect.addFlashAttribute("sucesso", "Vantagem atualizada com sucesso!");
            return "redirect:/empresa/vantagens";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "empresa/vantagem-form";
        }
    }

    // POST /empresa/vantagens/{id}/excluir — excluir
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            vantagemService.excluir(principal.getName(), id);
            redirect.addFlashAttribute("sucesso", "Vantagem excluída com sucesso!");
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/empresa/vantagens";
    }
}
