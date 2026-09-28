package prueba.despliegue.service;

import org.springframework.stereotype.Service;
import prueba.despliegue.dto.ProductoRequest;
import prueba.despliegue.dto.ProductoResponse;
import prueba.despliegue.exception.CodigoDuplicadoException;
import prueba.despliegue.exception.ProductoNoEncontradoException;
import prueba.despliegue.model.Producto;
import prueba.despliegue.repository.ProductoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ProductoService {
    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<ProductoResponse> listar(String busqueda) {
        String termino = busqueda == null ? "" : busqueda.strip().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(producto -> coincide(producto, termino))
                .sorted(Comparator.comparing(Producto::nombre, String.CASE_INSENSITIVE_ORDER))
                .map(ProductoResponse::from)
                .toList();
    }

    public ProductoResponse obtener(long id) {
        return ProductoResponse.from(encontrar(id));
    }

    public synchronized ProductoResponse crear(ProductoRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        verificarCodigoDisponible(codigo, 0);
        Producto producto = new Producto(
                0,
                codigo,
                request.nombre().strip(),
                request.categoria().strip(),
                request.precio(),
                request.stock()
        );
        return ProductoResponse.from(repository.save(producto));
    }

    public synchronized ProductoResponse actualizar(long id, ProductoRequest request) {
        encontrar(id);
        String codigo = normalizarCodigo(request.codigo());
        verificarCodigoDisponible(codigo, id);
        Producto producto = new Producto(
                id,
                codigo,
                request.nombre().strip(),
                request.categoria().strip(),
                request.precio(),
                request.stock()
        );
        return ProductoResponse.from(repository.save(producto));
    }

    public synchronized void eliminar(long id) {
        encontrar(id);
        repository.deleteById(id);
    }

    private Producto encontrar(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    private void verificarCodigoDisponible(String codigo, long idActual) {
        repository.findByCodigo(codigo)
                .filter(producto -> producto.id() != idActual)
                .ifPresent(producto -> {
                    throw new CodigoDuplicadoException(codigo);
                });
    }

    private static String normalizarCodigo(String codigo) {
        return codigo.strip().toUpperCase(Locale.ROOT);
    }

    private static boolean coincide(Producto producto, String termino) {
        return termino.isEmpty()
                || producto.codigo().toLowerCase(Locale.ROOT).contains(termino)
                || producto.nombre().toLowerCase(Locale.ROOT).contains(termino)
                || producto.categoria().toLowerCase(Locale.ROOT).contains(termino);
    }
}
