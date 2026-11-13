package pe.edu.upeu.PharmaBackend.auditoria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.upeu.PharmaBackend.service.service.AuditoriaService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaListener {
    private final AuditoriaService auditoria;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmar(EventoAuditoria evento) {
        try {
            auditoria.registrar(evento);
        } catch (RuntimeException ex) {
            log.error("No se pudo auditar {} {}", evento.accion(), evento.entidadId(), ex);
        }
    }
}
