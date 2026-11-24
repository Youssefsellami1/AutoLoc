package tn.esprit.autolocn.web.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.autolocn.exception.ResourceNotFoundException;

/**
 * Gestionnaire global des exceptions REST (Atelier 5, étendu Activité DTO).
 * Traduit les exceptions métier en réponses HTTP avec corps JSON :
 * - ResourceNotFoundException -> 404
 * - IllegalArgumentException -> 400
 * - MethodArgumentNotValidException (échec de validation d'un DTO) -> 400
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

    /**
     * Échec de validation d'un DTO ({@code @Valid} sur le contrôleur) :
     * 400 avec le détail des champs rejetés, même format JSON que les autres erreurs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "statut", HttpStatus.BAD_REQUEST.value(),
                "message", message,
                "horodatage", LocalDateTime.now(ZONE).toString()));
    }
}
