package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.auth.AsignarRolesRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.auth.CambioPasswordRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.auth.EstadoUsuarioRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.PharmaBackend.service.impl.GestionSeguridadService;

@RestController
@RequestMapping("/api/v1")
public class GestionSeguridadController {

    private final GestionSeguridadService service;

    public GestionSeguridadController(GestionSeguridadService service) {
        this.service = service;
    }

    @PutMapping("/auth/password")
    public ResponseEntity<Void> cambiarPassword(
            Authentication authentication,
            @Valid @RequestBody CambioPasswordRequestDTO request) {

        service.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/{id}/roles")
    public ResponseEntity<UsuarioResponseDTO> asignarRoles(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody AsignarRolesRequestDTO request) {

        return ResponseEntity.ok(
                service.asignarRoles(
                        id,
                        authentication.getName(),
                        request));
    }

    @PatchMapping("/usuarios/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody EstadoUsuarioRequestDTO request) {

        return ResponseEntity.ok(
                service.cambiarEstado(
                        id,
                        authentication.getName(),
                        request));
    }
}