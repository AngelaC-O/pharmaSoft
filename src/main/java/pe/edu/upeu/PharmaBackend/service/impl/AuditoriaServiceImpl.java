package pe.edu.upeu.PharmaBackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.auditoria.EventoAuditoria;
import pe.edu.upeu.PharmaBackend.dto.AuditoriaEventoDTO;
import pe.edu.upeu.PharmaBackend.dto.PaginaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.AuditoriaEvento;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.AuditoriaEventoRepository;
import pe.edu.upeu.PharmaBackend.service.service.AuditoriaService;
import pe.edu.upeu.PharmaBackend.util.PaginacionUtil;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {
    private final AuditoriaEventoRepository repositorio;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(EventoAuditoria evento) {
        var registro = new AuditoriaEvento();
        registro.setFecha(LocalDateTime.now());
        registro.setUsuario(evento.usuario());
        registro.setAccion(evento.accion());
        registro.setEntidad(evento.entidad());
        registro.setEntidadId(evento.entidadId());
        registro.setDetalle(evento.detalle());
        registro.setRequestId(evento.requestId());
        repositorio.save(registro);
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<AuditoriaEventoDTO> buscar(String usuario, String accion,
            LocalDate desde, LocalDate hasta, int pagina, int tamanio) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("El rango de fechas es inválido");
        }
        var pageable = PaginacionUtil.construir(pagina, tamanio, "fecha", "desc", Set.of("fecha"));
        return PaginaResponseDTO.de(repositorio.buscar(limpio(usuario), limpio(accion),
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(), pageable).map(AuditoriaEventoDTO::de));
    }

    private String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
