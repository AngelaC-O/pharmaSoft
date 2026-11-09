package pe.edu.upeu.PharmaBackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.auth.AsignarRolesRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.auth.CambioPasswordRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.auth.EstadoUsuarioRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Rol;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.mapper.UsuarioMapper;
import pe.edu.upeu.PharmaBackend.repository.RolRepository;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GestionSeguridadService {

    private static final Logger log =
            LoggerFactory.getLogger(GestionSeguridadService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public GestionSeguridadService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public void cambiarPassword(
            String username,
            CambioPasswordRequestDTO request) {

        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + username));

        if (!passwordEncoder.matches(
                request.getPasswordActual(), usuario.getPassword())) {
            throw new ReglaNegocioException(
                    "La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(
                request.getPasswordNueva(), usuario.getPassword())) {
            throw new ReglaNegocioException(
                    "La nueva contraseña debe ser distinta de la actual");
        }

        if (request.getPasswordNueva()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException(
                    "La nueva contraseña no puede superar 72 bytes");
        }

        usuario.setPassword(
                passwordEncoder.encode(request.getPasswordNueva()));

        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada: usuario={}", username);
    }

    @Transactional
    public UsuarioResponseDTO asignarRoles(
            Long id,
            String adminUsername,
            AsignarRolesRequestDTO request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con id: " + id));

        List<Rol> roles = rolRepository.findAllById(request.getRolIds());

        if (roles.size() != request.getRolIds().size()) {
            Set<Long> encontrados = roles.stream()
                    .map(Rol::getId)
                    .collect(Collectors.toSet());

            Set<Long> faltantes = new HashSet<>(request.getRolIds());
            faltantes.removeAll(encontrados);

            throw new RecursoNoEncontradoException(
                    "Roles no encontrados: " + faltantes);
        }

        boolean teniaAdmin = usuario.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));

        boolean conservaraAdmin = roles.stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNombre()));

        if (usuario.getUsername().equalsIgnoreCase(adminUsername)
                && teniaAdmin && !conservaraAdmin) {
            throw new ReglaNegocioException(
                    "Un ADMIN no puede quitarse a sí mismo el rol ADMIN");
        }

        usuario.setRoles(new HashSet<>(roles));
        Usuario guardado = usuarioRepository.save(usuario);

        log.info("Roles actualizados: usuario={} roles={}",
                usuario.getUsername(),
                roles.stream().map(Rol::getNombre).toList());

        return usuarioMapper.toUsuarioResponse(guardado);
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstado(
            Long id,
            String adminUsername,
            EstadoUsuarioRequestDTO request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado con id: " + id));

        if (usuario.getUsername().equalsIgnoreCase(adminUsername)
                && Boolean.FALSE.equals(request.getEstado())) {
            throw new ReglaNegocioException(
                    "Un ADMIN no puede desactivarse a sí mismo");
        }

        usuario.setEstado(request.getEstado());
        Usuario guardado = usuarioRepository.save(usuario);

        log.info("Estado actualizado: usuario={} estado={}",
                usuario.getUsername(), request.getEstado());

        return usuarioMapper.toUsuarioResponse(guardado);
    }
}