package pe.edu.upeu.PharmaBackend.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.auth.*;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.PharmaBackend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;
import pe.edu.upeu.PharmaBackend.security.*;
import pe.edu.upeu.PharmaBackend.service.service.AuthService;

import java.util.List;
import pe.edu.upeu.PharmaBackend.auditoria.EventoAuditoria;
import pe.edu.upeu.PharmaBackend.service.service.AuditoriaService;
import org.springframework.security.core.AuthenticationException;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    private final AuditoriaService auditoria;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper, AuditoriaService auditoria) {
        this.auditoria = auditoria;

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {

        String username = request.getUsername().trim();

        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        } catch (AuthenticationException ex) {
            auditoria.registrar(EventoAuditoria.login("LOGIN_FALLIDO", username));
            throw ex;
        }

        UserDetails principal =
                (UserDetails) autenticacion.getPrincipal();

        Usuario usuario = buscar(principal.getUsername());

        String token = jwtService.generarToken(principal);

        List<String> roles =
                usuarioMapper.nombresRoles(usuario.getRoles());

        auditoria.registrar(EventoAuditoria.login("LOGIN_EXITOSO", username));
        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtService.getExpiracionSegundos(),
                usuario.getUsername(),
                usuario.getEmpleado().getNombreCompleto(),
                roles,
                PermisosPorRol.permisos(usuario.getRoles())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO usuarioActual(String username) {
        return usuarioMapper.toUsuarioResponse(buscar(username));
    }

    private Usuario buscar(String username) {
        return usuarioRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Usuario no encontrado: " + username
                        ));
    }
}