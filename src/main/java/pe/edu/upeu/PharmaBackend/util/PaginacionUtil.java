package pe.edu.upeu.PharmaBackend.util;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;

public final class PaginacionUtil {
    private PaginacionUtil() {}

    public static PageRequest construir(int pagina, int tamanio, String campo, String direccion, Set<String> campos) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100 || (long) pagina * tamanio > Integer.MAX_VALUE) {
            throw new ReglaNegocioException("Página inválida o tamaño fuera del rango 1 a 100");
        }
        if (!campos.contains(campo)) {
            throw new ReglaNegocioException("Campo de orden no permitido: " + campo);
        }
        if (!"asc".equalsIgnoreCase(direccion) && !"desc".equalsIgnoreCase(direccion)) {
            throw new ReglaNegocioException("La dirección debe ser asc o desc");
        }
        return PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.fromString(direccion), campo)
                .and(Sort.by("id")));
    }
}
