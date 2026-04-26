package com.flacofitness.app.exception;

import com.flacofitness.app.controller.AsistenciaController;
import com.flacofitness.app.controller.AccessController;
import com.flacofitness.app.controller.PagoController;
import com.flacofitness.app.controller.RutinaController;
import com.flacofitness.app.controller.UsuarioController;
import com.flacofitness.app.controller.ViewController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@ControllerAdvice(assignableTypes = {
        ViewController.class,
        UsuarioController.class,
        PagoController.class,
        RutinaController.class,
        AsistenciaController.class,
        AccessController.class
})
public class GlobalViewExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalViewExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        String message = Objects.requireNonNullElse(ex.getMessage(), "El recurso solicitado no esta disponible.");
        String requestUri = Objects.requireNonNullElse(request.getRequestURI(), "/");
        return construirVistaError(
                "error/404",
                "Recurso no encontrado",
                message,
                "La ruta o el registro solicitado no esta disponible.",
                requestUri
        );
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ModelAndView handleBusinessValidation(BusinessValidationException ex,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String message = Objects.requireNonNullElse(ex.getMessage(), "La operacion no pudo completarse.");
        String requestUri = Objects.requireNonNullElse(request.getRequestURI(), "/");
        return construirVistaError(
                "error/400",
                "Operacion no valida",
                message,
                "Revisa la informacion enviada y vuelve a intentarlo.",
                requestUri
        );
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        String requestUri = Objects.requireNonNullElse(request.getRequestURI(), "/");
        LOGGER.error("Error no controlado en una vista MVC. Ruta: {}", requestUri, ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return construirVistaError(
                "error/500",
                "Se produjo un error interno",
                "La aplicacion encontro un problema al procesar la solicitud.",
                "Puedes volver al dashboard o reintentar la operacion desde el modulo correspondiente.",
                requestUri
        );
    }

        private ModelAndView construirVistaError(@NonNull String vista,
                                                                                         @NonNull String titulo,
                                                                                         @NonNull String mensaje,
                                                                                         @NonNull String ayuda,
                                                                                         @NonNull String ruta) {
        ModelAndView modelAndView = new ModelAndView(vista);
        modelAndView.addObject("errorTitle", titulo);
        modelAndView.addObject("errorMessage", mensaje);
        modelAndView.addObject("errorHint", ayuda);
        modelAndView.addObject("errorPath", ruta);
        return modelAndView;
    }
}
