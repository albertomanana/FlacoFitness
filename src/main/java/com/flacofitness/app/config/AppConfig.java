package com.flacofitness.app.config;

import com.flacofitness.app.security.AccessGuardInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final String uploadDir;
    @NonNull
    private final AccessGuardInterceptor accessGuardInterceptor;

    public AppConfig(@Value("${upload.dir}") String uploadDir,
                     @NonNull AccessGuardInterceptor accessGuardInterceptor) {
        this.uploadDir = uploadDir;
        this.accessGuardInterceptor = accessGuardInterceptor;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(Objects.requireNonNull(accessGuardInterceptor))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/acceso",
                        "/salir",
                        "/error",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/vendor/**",
                        "/uploads/**",
                        "/favicon.ico"
                );
    }
}
