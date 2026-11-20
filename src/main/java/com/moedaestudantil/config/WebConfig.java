package com.moedaestudantil.config;

import com.moedaestudantil.service.FotoStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Expõe as fotos das vantagens (pasta de upload) em /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FotoStorageService fotoStorage;

    public WebConfig(FotoStorageService fotoStorage) {
        this.fotoStorage = fotoStorage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fotoStorage.getDiretorio().toUri().toString());
    }
}
