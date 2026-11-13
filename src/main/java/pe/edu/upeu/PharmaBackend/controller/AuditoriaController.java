package pe.edu.upeu.PharmaBackend.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.AuditoriaEventoDTO;
import pe.edu.upeu.PharmaBackend.dto.PaginaResponseDTO;
import pe.edu.upeu.PharmaBackend.service.service.AuditoriaService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {
    private final AuditoriaService auditoria;

    @GetMapping
    public PaginaResponseDTO<AuditoriaEventoDTO> buscar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        return auditoria.buscar(usuario, accion, desde, hasta, pagina, tamanio);
    }
}
