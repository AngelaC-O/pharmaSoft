package pe.edu.upeu.PharmaBackend.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PharmaBackend.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, long id);

    List<Producto> findByCategoriaId(Long categoriaId);
    boolean existsByCategoriaId(Long categoriaId);
    @Query(value = """
            select p from Producto p join fetch p.categoria where (:categoriaId is null or p.categoria.id = :categoriaId) and (:texto is null or lower(p.nombre) like lower(concat('%', :texto, '%'))) and (:disponible = false or (p.estado = true and p.stock > 0))
            """, countQuery = """
            select count(p) from Producto p join p.categoria where (:categoriaId is null or p.categoria.id = :categoriaId) and (:texto is null or lower(p.nombre) like lower(concat('%', :texto, '%'))) and (:disponible = false or (p.estado = true and p.stock > 0))
            """)
    Page<Producto> buscarPagina(Long categoriaId, String texto, boolean disponible, Pageable pagina);
}
