package pe.edu.upeu.PharmaBackend.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import static pe.edu.upeu.PharmaBackend.config.RedisCacheConfig.CATEGORIAS;
import static pe.edu.upeu.PharmaBackend.config.RedisCacheConfig.PRODUCTOS;
import static pe.edu.upeu.PharmaBackend.config.RedisCacheConfig.PRODUCTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.CategoriaResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;

import java.util.Optional;
@Service
public class CategoriaServiceImpl implements CategoriaService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CATEGORIAS, allEntries = true)
    public CategoriaResponseDTO create(CategoriaRequestDTO t) {
        String nombre = t.getNombre().trim();
        if(categoriaRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException(
                    "Ya existe una catgoria con el nombre "+ nombre
            );
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(t.getDescripcion());
        categoria.setEstado(t.getEstado());

        Categoria catCreada = categoriaRepository.saveAndFlush(categoria);

        return convertirResponse(catCreada);
    }
    @Transactional
    @Override
    @CacheEvict(cacheNames = {CATEGORIAS, PRODUCTOS, PRODUCTO}, allEntries = true)
    public CategoriaResponseDTO update(Long aLong, CategoriaRequestDTO t) {
        Categoria categoria= categoriaRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Categoria no encontrada con el Id "+ aLong
                )
        );
        categoria.setNombre(t.getNombre());
        categoria.setDescripcion(t.getDescripcion());
        categoria.setEstado(t.getEstado());

        Categoria catActualizada = categoriaRepository.saveAndFlush(categoria);

        return convertirResponse(catActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO read(Long aLong) {
        Categoria categoria= categoriaRepository.findById(aLong)
                .orElseThrow(()->
                        new RecursosNoEncontradoException(
                                "Categoria no encontrada con el Id "+ aLong
                        )
                );
        return convertirResponse(categoria);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CATEGORIAS, allEntries = true)
    public void delete(Long aLong) {
        Categoria categoria = categoriaRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Categoria no encontrada con el Id "+ aLong
                )
        );
        categoriaRepository.delete(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CATEGORIAS, key = "'lista'")
    public Iterable<CategoriaResponseDTO> readAll() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }
    private CategoriaResponseDTO convertirResponse(Categoria categoria){
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getEstado(),
                categoria.getFechaCreacion(),
                categoria.getFechaModificacion(),
                categoria.getCreadoPor(),
                categoria.getModificadoPor()
        );
    }
}
