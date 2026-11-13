package pe.edu.upeu.PharmaBackend.auditoria;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;

public record EventoAuditoria(String usuario, String accion, String entidad, Long entidadId,
        String detalle, String requestId) {
    public static EventoAuditoria de(String accion, String entidad, Long id, String detalle) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return new EventoAuditoria(auth == null ? "sistema" : auth.getName(), accion, entidad,
                id, limitar(detalle, 500), MDC.get("requestId"));
    }

    public static EventoAuditoria login(String accion, String usuario) {
        return new EventoAuditoria(limitar(usuario, 50), accion, "Usuario", null, null, MDC.get("requestId"));
    }

    private static String limitar(String valor, int limite) {
        return valor == null || valor.length() <= limite ? valor : valor.substring(0, limite);
    }
}
