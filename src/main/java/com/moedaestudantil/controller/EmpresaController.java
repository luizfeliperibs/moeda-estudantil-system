package com.moedaestudantil.controller;

import com.moedaestudantil.dto.EmpresaForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.EmpresaParceira;
import com.moedaestudantil.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller do CRUD de EmpresaParceira.
 * Rotas conforme a seção 3 do docs/sprint2.md.
 */
@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    // GET /empresas — listar
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empresas", empresaService.listarTodas());
        return "empresa/lista";
    }

    // GET /empresas/novo — formulário de cadastro
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("form", new EmpresaForm());
        return "empresa/form";
    }

    // POST /empresas — criar
    @PostMapping
    public String criar(@Valid @ModelAttribute("form") EmpresaForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "empresa/form";
        }
        try {
            empresaService.criar(form);
            redirect.addFlashAttribute("sucesso", "Empresa parceira cadastrada com sucesso!");
            return "redirect:/empresas";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "empresa/form";
        }
    }

    // GET /empresas/{id}/editar — formulário de edição
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        EmpresaParceira empresa = empresaService.buscarPorId(id);
        EmpresaForm form = new EmpresaForm();
        form.setId(empresa.getId());
        form.setNome(empresa.getNome());
        form.setCnpj(empresa.getCnpj());
        form.setEmail(empresa.getEmail());
        model.addAttribute("form", form);
        return "empresa/form";
    }

    // POST /empresas/{id} — atualizar
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") EmpresaForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "empresa/form";
        }
        try {
            empresaService.atualizar(id, form);
            redirect.addFlashAttribute("sucesso", "Empresa parceira atualizada com sucesso!");
            return "redirect:/empresas";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "empresa/form";
        }
    }

    // GET /empresas/{id}/excluir — confirmação
    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(@PathVariable Long id, Model model) {
        model.addAttribute("empresa", empresaService.buscarPorId(id));
        return "empresa/confirmar-exclusao";
    }

    // POST /empresas/{id}/excluir — excluir
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            empresaService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Empresa parceira excluída com sucesso!");
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/empresas";
    }
}
