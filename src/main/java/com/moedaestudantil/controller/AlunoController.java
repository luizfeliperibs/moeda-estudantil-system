package com.moedaestudantil.controller;

import com.moedaestudantil.dto.AlunoForm;
import com.moedaestudantil.exception.RegraDeNegocioException;
import com.moedaestudantil.model.Aluno;
import com.moedaestudantil.service.AlunoService;
import com.moedaestudantil.service.InstituicaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller do CRUD de Aluno.
 * Rotas conforme a seção 3 do docs/sprint2.md.
 * NÃO acessa repositório diretamente — delega ao AlunoService.
 */
@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final InstituicaoService instituicaoService;

    public AlunoController(AlunoService alunoService, InstituicaoService instituicaoService) {
        this.alunoService = alunoService;
        this.instituicaoService = instituicaoService;
    }

    // GET /alunos — listar
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("alunos", alunoService.listarTodos());
        return "aluno/lista";
    }

    // GET /alunos/novo — formulário de cadastro
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("form", new AlunoForm());
        model.addAttribute("instituicoes", instituicaoService.listarTodas());
        return "aluno/form";
    }

    // POST /alunos — criar
    @PostMapping
    public String criar(@Valid @ModelAttribute("form") AlunoForm form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("instituicoes", instituicaoService.listarTodas());
            return "aluno/form";
        }
        try {
            alunoService.criar(form);
            redirect.addFlashAttribute("sucesso", "Aluno cadastrado com sucesso!");
            return "redirect:/alunos";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("instituicoes", instituicaoService.listarTodas());
            return "aluno/form";
        }
    }

    // GET /alunos/{id}/editar — formulário de edição
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Aluno aluno = alunoService.buscarPorId(id);
        AlunoForm form = new AlunoForm();
        form.setId(aluno.getId());
        form.setNome(aluno.getNome());
        form.setEmail(aluno.getEmail());
        form.setCpf(aluno.getCpf());
        form.setRg(aluno.getRg());
        form.setEndereco(aluno.getEndereco());
        form.setCurso(aluno.getCurso());
        form.setInstituicaoId(aluno.getInstituicao() != null ? aluno.getInstituicao().getId() : null);
        model.addAttribute("form", form);
        model.addAttribute("instituicoes", instituicaoService.listarTodas());
        return "aluno/form";
    }

    // POST /alunos/{id} — atualizar
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("form") AlunoForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("instituicoes", instituicaoService.listarTodas());
            return "aluno/form";
        }
        try {
            alunoService.atualizar(id, form);
            redirect.addFlashAttribute("sucesso", "Aluno atualizado com sucesso!");
            return "redirect:/alunos";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("instituicoes", instituicaoService.listarTodas());
            return "aluno/form";
        }
    }

    // GET /alunos/{id}/excluir — confirmação de exclusão
    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(@PathVariable Long id, Model model) {
        model.addAttribute("aluno", alunoService.buscarPorId(id));
        return "aluno/confirmar-exclusao";
    }

    // POST /alunos/{id}/excluir — excluir
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            alunoService.excluir(id);
            redirect.addFlashAttribute("sucesso", "Aluno excluído com sucesso!");
        } catch (RegraDeNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/alunos";
    }
}
