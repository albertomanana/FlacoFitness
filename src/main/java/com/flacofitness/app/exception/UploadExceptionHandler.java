package com.flacofitness.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@ControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensajeError", "La imagen supera el tamano maximo permitido.");
        String referer = request.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            return "redirect:/usuarios";
        }

        URI refererUri = URI.create(referer);
        String redirectPath = refererUri.getPath();

        if (refererUri.getQuery() != null && !refererUri.getQuery().isBlank()) {
            redirectPath = redirectPath + "?" + refererUri.getQuery();
        }

        return "redirect:" + redirectPath;
    }
}
