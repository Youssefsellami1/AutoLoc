package tn.esprit.autolocn.web.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.autolocn.exception.ResourceNotFoundException;

/**
 * Gestionnaire global des exceptions REST (Atelier 5).
 * Traduit les exceptions métier en réponses HTTP avec corps JSON :
 * - ResourceNotFoundException -> 404
 * - IllegalArgumentException -> 400
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Fuseau horaire explicite — règle SonarQube : ne jamais appeler .now() sans ZoneId ni Clock. */
    private static final ZoneId ZONE = ZoneId.systemDefault();

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "statut", HttpStatus.NOT_FOUND.value(),
                "message", ex.getMessage(),
                "horodatage", LocalDateTime.now(ZONE).toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "statut", HttpStatus.BAD_REQUEST.value(),
                "message", ex.getMessage(),
                "horodatage", LocalDateTime.now(ZONE).toString()));
    }
}
