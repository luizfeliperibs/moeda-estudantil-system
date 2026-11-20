package com.moedaestudantil.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO de formulário para cadastro/edição de Vantagem (com foto).
 */
public class VantagemForm {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres")
    private String nome;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Informe o custo em moedas")
    @Min(value = 1, message = "O custo deve ser maior que zero")
    private Integer custoMoedas;

    private MultipartFile foto;

    /** URL da foto atual (somente leitura, usada na edição). */
    private String fotoUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getCustoMoedas() { return custoMoedas; }
    public void setCustoMoedas(Integer custoMoedas) { this.custoMoedas = custoMoedas; }

    public MultipartFile getFoto() { return foto; }
    public void setFoto(MultipartFile foto) { this.foto = foto; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
