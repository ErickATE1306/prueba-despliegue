package prueba.despliegue.repository;

import org.springframework.stereotype.Repository;
import prueba.despliegue.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductoRepository implements ProductoRepository {
    private final ConcurrentHashMap<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(productos.values());
    }

    @Override
    public Optional<Producto> findById(long id) {
        return Optional.ofNullable(productos.get(id));
    }

    @Override
    public Optional<Producto> findByCodigo(String codigo) {
        return productos.values().stream()
                .filter(producto -> producto.codigo().equalsIgnoreCase(codigo))
                .findFirst();
    }

    @Override
    public Producto save(Producto producto) {
        long id = producto.id() == 0 ? secuencia.incrementAndGet() : producto.id();
        Producto guardado = new Producto(
                id,
                producto.codigo(),
                producto.nombre(),
                producto.categoria(),
                producto.precio(),
                producto.stock()
        );
        productos.put(id, guardado);
        return guardado;
    }

    @Override
    public void deleteById(long id) {
        productos.remove(id);
    }
}
