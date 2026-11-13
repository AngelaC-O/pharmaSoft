package pe.edu.upeu.PharmaBackend.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.upeu.PharmaBackend.entity.AuditoriaEvento;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long> {
    @Query("""
            select a from AuditoriaEvento a
            where (:usuario is null or upper(a.usuario) = upper(:usuario))
              and (:accion is null or a.accion = :accion)
              and (:desde is null or a.fecha >= :desde)
              and (:hasta is null or a.fecha < :hasta)
            """)
    Page<AuditoriaEvento> buscar(String usuario, String accion, LocalDateTime desde,
            LocalDateTime hasta, Pageable pagina);
}
