package com.comfenalco.cine.exception;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public String reglaNegocio(ReglaNegocioException ex, RedirectAttributes flash,
                               HttpServletRequest request) {
        flash.addFlashAttribute("error", ex.getMessage());
        return volverAtras(request);
    }

    // Duplicados que se saltaron la comprobación previa, bloqueos y conflictos de versión
    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    public String conflictoDeDatos(Exception ex, RedirectAttributes flash,
                                   HttpServletRequest request) {
        // El mensaje de la BD puede traer valores reales (un usuario, un correo): solo en DEBUG
        log.warn("Conflicto de datos: {}", ex.getClass().getSimpleName());
        log.debug("Detalle del conflicto", ex);

        flash.addFlashAttribute("error",
                "La operación entró en conflicto con otra hecha al mismo tiempo. Revisa e intenta de nuevo");
        return volverAtras(request);
    }

    // Vuelve a la página anterior usando solo su ruta, nunca el host: evita redirecciones a sitios externos
    private String volverAtras(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                String ruta = uri.getPath();

                // "//sitio.com" sería una URL externa disfrazada: solo se aceptan rutas propias
                if (ruta != null && ruta.startsWith("/") && !ruta.startsWith("//")) {
                    return "redirect:" + ruta + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
                }
            } catch (IllegalArgumentException ignorada) {
                // Referer malformado: se usa la ruta por defecto
            }
        }
        return "redirect:/";
    }
}