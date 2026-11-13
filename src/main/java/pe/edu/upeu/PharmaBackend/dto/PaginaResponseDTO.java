package pe.edu.upeu.PharmaBackend.dto;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponseDTO<T>(List<T> contenido, int pagina, int tamanio,
        long totalElementos, int totalPaginas, boolean primera, boolean ultima) implements Serializable {
    public static <T> PaginaResponseDTO<T> de(Page<T> pagina) {
        return new PaginaResponseDTO<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(), pagina.isLast());
    }
}
