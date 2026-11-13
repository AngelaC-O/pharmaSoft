package pe.edu.upeu.PharmaBackend.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;


import pe.edu.upeu.PharmaBackend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDni(String dni);
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    @Query(value = """
            select c from Cliente c where (:texto is null or lower(concat(c.nombres, ' ', c.apellidos)) like lower(concat('%', :texto, '%')) or c.dni like concat('%', :texto, '%')) and (:disponible = false or c.estado = true)
            """, countQuery = """
            select count(c) from Cliente c where (:texto is null or lower(concat(c.nombres, ' ', c.apellidos)) like lower(concat('%', :texto, '%')) or c.dni like concat('%', :texto, '%')) and (:disponible = false or c.estado = true)
            """)
    Page<Cliente> buscarPagina(String texto, boolean disponible, Pageable pagina);
}

