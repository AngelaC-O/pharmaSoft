package pe.edu.upeu.PharmaBackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "auditoria_eventos")
@Getter
@Setter
public class AuditoriaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(nullable = false, length = 50)
    private String usuario;
    @Column(nullable = false, length = 40)
    private String accion;
    @Column(nullable = false, length = 40)
    private String entidad;
    private Long entidadId;
    @Column(length = 500)
    private String detalle;
    @Column(length = 36)
    private String requestId;
}
