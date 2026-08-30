package pe.edu.upeu.PharmaBackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pe.edu.upeu.PharmaBackend.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalExceptionHandler {
    @ExceptionHandler(RecursosNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            RecursosNoEncontradoException ex,
            HttpServletRequest request){

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
        null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(
            ReglaNegocioException ex,
            HttpServletRequest request) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

        @ExceptionHandler(MethodArgumentNotValidException.class)
                public ResponseEntity<ErrorResponseDTO> handleValidation(
                        MethodArgumentNotValidException ex,
        HttpServletRequest request){
            Map<String, String> validationErrors= new LinkedHashMap<>();

            ex.getBindingResult().getFieldErrors().forEach(errors ->
                    validationErrors.put(errors.getField(), errors.getDefaultMessage()
                    )
            );
            ErrorResponseDTO error = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "Existen errores de validacion",
                    request.getRequestURI(),
                    validationErrors
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
}
