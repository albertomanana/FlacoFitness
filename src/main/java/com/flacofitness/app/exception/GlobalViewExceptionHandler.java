package com.flacofitness.app.exception;

import com.flacofitness.app.controller.AsistenciaController;
import com.flacofitness.app.controller.PagoController;
import com.flacofitness.app.controller.RutinaController;
import com.flacofitness.app.controller.UsuarioController;
import com.flacofitness.app.controller.ViewController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {
        ViewController.class,
        UsuarioController.class,
        PagoController.class,
        RutinaController.class,
        AsistenciaController.class
})
public class GlobalViewExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalViewExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return construirVistaError(
                "error/404",
                "Recurso no encontrado",
                ex.getMessage(),
                "La ruta o el registro solicitado no esta disponible.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ModelAndView handleBusinessValidation(BusinessValidationException ex,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return construirVistaError(
                "error/400",
                "Operacion no valida",
                ex.getMessage(),
                "Revisa la informacion enviada y vuelve a intentarlo.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        LOGGER.error("Error no controlado en una vista MVC. Ruta: {}", request.getRequestURI(), ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return construirVistaError(
                "error/500",
                "Se produjo un error interno",
                "La aplicacion encontro un problema al procesar la solicitud.",
                "Puedes volver al dashboard o reintentar la operacion desde el modulo correspondiente.",
                request.getRequestURI()
        );
    }

    private ModelAndView construirVistaError(String vista,
                                             String titulo,
                                             String mensaje,
                                             String ayuda,
                                             String ruta) {
        ModelAndView modelAndView = new ModelAndView(vista);
        modelAndView.addObject("errorTitle", titulo);
        modelAndView.addObject("errorMessage", mensaje);
        modelAndView.addObject("errorHint", ayuda);
        modelAndView.addObject("errorPath", ruta);
        return modelAndView;
    }
}
