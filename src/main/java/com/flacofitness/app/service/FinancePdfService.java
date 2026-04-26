package com.flacofitness.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class FinancePdfService {

    private final SpringTemplateEngine templateEngine;

    public FinancePdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        String html = templateEngine.process(templateName, context);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, buildBaseUrl());
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el PDF financiero", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF financiero", ex);
        }
    }

    private String buildBaseUrl() {
        return Paths.get(".").toAbsolutePath().normalize().toUri().toString();
    }
}
