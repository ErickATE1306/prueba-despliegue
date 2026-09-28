package prueba.despliegue.repository;

import prueba.despliegue.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> findAll();

    Optional<Producto> findById(long id);

    Optional<Producto> findByCodigo(String codigo);

    Producto save(Producto producto);

    void deleteById(long id);
}
