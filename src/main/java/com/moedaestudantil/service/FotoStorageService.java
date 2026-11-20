package com.moedaestudantil.service;

import com.moedaestudantil.exception.RegraDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Salva fotos das vantagens em disco. O nome do arquivo é gerado pelo servidor
 * (UUID + extensão derivada do tipo aceito), nunca vindo do cliente.
 */
@Service
public class FotoStorageService {

    private static final long TAMANHO_MAXIMO = 2L * 1024 * 1024;
    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path diretorio;

    public FotoStorageService(@Value("${app.upload-dir}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public Path getDiretorio() {
        return diretorio;
    }

    /** Salva a foto e devolve a URL pública ({@code /uploads/<arquivo>}). */
    public String salvar(MultipartFile foto) {
        String extensao = EXTENSOES.get(foto.getContentType());
        if (extensao == null) {
            throw new RegraDeNegocioException("A foto deve ser uma imagem JPG, PNG, WEBP ou GIF.");
        }
        if (foto.getSize() > TAMANHO_MAXIMO) {
            throw new RegraDeNegocioException("A foto deve ter no máximo 2 MB.");
        }
        String nome = UUID.randomUUID() + extensao;
        try {
            byte[] bytes = foto.getBytes();
            if (!assinaturaConfere(foto.getContentType(), bytes)) {
                throw new RegraDeNegocioException("O conteúdo do arquivo não corresponde a uma imagem válida.");
            }
            Files.createDirectories(diretorio);
            Files.write(diretorio.resolve(nome), bytes);
        } catch (IOException e) {
            throw new RegraDeNegocioException("Não foi possível salvar a foto.");
        }
        return "/uploads/" + nome;
    }

    /** O Content-Type vem do cliente; confere os primeiros bytes (assinatura) do arquivo. */
    private boolean assinaturaConfere(String tipo, byte[] b) {
        return switch (tipo) {
            case "image/jpeg" -> b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/png" -> b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/gif" -> b.length > 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
            case "image/webp" -> b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }
}
