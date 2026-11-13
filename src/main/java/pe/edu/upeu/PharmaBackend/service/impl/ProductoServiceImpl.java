package pe.edu.upeu.PharmaBackend.service.impl;
import pe.edu.upeu.PharmaBackend.dto.PaginaResponseDTO;
import pe.edu.upeu.PharmaBackend.util.PaginacionUtil;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import static pe.edu.upeu.PharmaBackend.config.RedisCacheConfig.PRODUCTOS;
import static pe.edu.upeu.PharmaBackend.config.RedisCacheConfig.PRODUCTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.upeu.PharmaBackend.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.ProductoRequestDTO;
import pe.edu.upeu.PharmaBackend.dto.ProductoResponseDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.entity.Producto;
import pe.edu.upeu.PharmaBackend.exception.RecursosNoEncontradoException;
import pe.edu.upeu.PharmaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.service.service.ProductoService;


@Service
public class ProductoServiceImpl implements ProductoService {
    private final ApplicationEventPublisher eventos;
    private static final Logger LOG = LoggerFactory.getLogger(ProductoServiceImpl.class);

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository, ApplicationEventPublisher eventos) {
        this.eventos = eventos;
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = PRODUCTOS, allEntries = true)
    public ProductoResponseDTO create(ProductoRequestDTO t) {
        String nombre = t.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCase(nombre)){
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre "+ nombre
            );
        }

        Categoria categoria = buscarCategoria(t.getCategoriaId());

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(t.getPrecio());
        producto.setStock(t.getStock());
        producto.setEstado(t.getEstado());
        producto.setCategoria(categoria);

        Producto prodCreado = productoRepository.save(producto);

        return convertirResponse(prodCreado);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {PRODUCTOS, PRODUCTO}, allEntries = true)
    public ProductoResponseDTO update(Long aLong, ProductoRequestDTO t) {
        Producto producto = productoRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Producto no encontrado con id: "+ aLong
                )
        );

        String nombre = t.getNombre().trim();
        if(productoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, aLong)){
            throw new ReglaNegocioException(
                    "Ya existe un producto con el nombre "+ nombre
            );
        }

        Categoria categoria = buscarCategoria(t.getCategoriaId());

        producto.setNombre(nombre);
        var precioAnterior = producto.getPrecio();
        producto.setPrecio(t.getPrecio());
        producto.setStock(t.getStock());
        producto.setEstado(t.getEstado());
        producto.setCategoria(categoria);

        Producto prodActualizado = productoRepository.saveAndFlush(producto);

        if (precioAnterior.compareTo(producto.getPrecio()) != 0) {
            eventos.publishEvent(EventoAuditoria.de("PRECIO_MODIFICADO", "Producto",
                    producto.getId(), precioAnterior + " -> " + producto.getPrecio()));
        }
        return convertirResponse(prodActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = PRODUCTO, key = "#p0")
    public ProductoResponseDTO read(Long aLong) {
        Producto producto = productoRepository.findById(aLong)
                .orElseThrow(()->
                        new RecursosNoEncontradoException(
                                "Producto no encontrado con id: "+ aLong
                        )
                );
        return convertirResponse(producto);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {PRODUCTOS, PRODUCTO}, allEntries = true)
    public void delete(Long aLong) {
        Producto producto = productoRepository.findById(aLong).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Producto no encontrado con id: "+ aLong
                )
        );
        productoRepository.delete(producto);
        eventos.publishEvent(EventoAuditoria.de("PRODUCTO_ELIMINADO", "Producto",
                producto.getId(), producto.getNombre()));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = PRODUCTOS, key = "'lista'")
    public Iterable<ProductoResponseDTO> readAll() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    private Categoria buscarCategoria(Long categoriaId){
        return categoriaRepository.findById(categoriaId).orElseThrow(()->
                new RecursosNoEncontradoException(
                        "Categoria no encontrada con id: "+ categoriaId
                )
        );
    }

    private ProductoResponseDTO convertirResponse(Producto producto){
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getEstado(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion(),
                producto.getCreadoPor(),
                producto.getModificadoPor()
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = PRODUCTOS)
    public PaginaResponseDTO<ProductoResponseDTO> listarPagina(int pagina, int tamanio, String ordenarPor, String direccion, Long categoriaId, String texto, boolean disponible) {
        var solicitud = PaginacionUtil.construir(pagina, tamanio, ordenarPor, direccion, Set.of("nombre", "precio", "stock", "id"));
        texto = texto == null || texto.isBlank() ? null : texto.trim();
        return PaginaResponseDTO.de(productoRepository.buscarPagina(categoriaId, texto, disponible, solicitud).map(this::convertirResponse));
    }
}
