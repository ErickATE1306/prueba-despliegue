package prueba.despliegue.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import prueba.despliegue.model.Producto;
import prueba.despliegue.exception.CodigoDuplicadoException;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProductoRepository implements ProductoRepository {
    private static final RowMapper<Producto> PRODUCTO_MAPPER = (rs, rowNum) -> new Producto(
            rs.getLong("id"),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("categoria"),
            rs.getBigDecimal("precio"),
            rs.getInt("stock")
    );

    private final JdbcTemplate jdbc;

    public JdbcProductoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Producto> findAll() {
        return jdbc.query("SELECT id, codigo, nombre, categoria, precio, stock FROM productos", PRODUCTO_MAPPER);
    }

    @Override
    public Optional<Producto> findById(long id) {
        return jdbc.query(
                "SELECT id, codigo, nombre, categoria, precio, stock FROM productos WHERE id = ?",
                PRODUCTO_MAPPER,
                id
        ).stream().findFirst();
    }

    @Override
    public Optional<Producto> findByCodigo(String codigo) {
        return jdbc.query(
                "SELECT id, codigo, nombre, categoria, precio, stock FROM productos WHERE UPPER(codigo) = UPPER(?)",
                PRODUCTO_MAPPER,
                codigo
        ).stream().findFirst();
    }

    @Override
    public Producto save(Producto producto) {
        if (producto.id() != 0) {
            try {
                jdbc.update(
                        "UPDATE productos SET codigo = ?, nombre = ?, categoria = ?, precio = ?, stock = ? WHERE id = ?",
                        producto.codigo(), producto.nombre(), producto.categoria(), producto.precio(), producto.stock(), producto.id()
                );
            } catch (DuplicateKeyException exception) {
                throw new CodigoDuplicadoException(producto.codigo());
            }
            return producto;
        }

        KeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO productos (codigo, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)",
                        new String[] {"id"}
                );
                statement.setString(1, producto.codigo());
                statement.setString(2, producto.nombre());
                statement.setString(3, producto.categoria());
                statement.setBigDecimal(4, producto.precio());
                statement.setInt(5, producto.stock());
                return statement;
            }, keys);
        } catch (DuplicateKeyException exception) {
            throw new CodigoDuplicadoException(producto.codigo());
        }
        Number id = keys.getKey();
        if (id == null) {
            throw new IllegalStateException("PostgreSQL no devolvio el ID del producto creado");
        }
        return new Producto(id.longValue(), producto.codigo(), producto.nombre(), producto.categoria(), producto.precio(), producto.stock());
    }

    @Override
    public void deleteById(long id) {
        jdbc.update("DELETE FROM productos WHERE id = ?", id);
    }
}
