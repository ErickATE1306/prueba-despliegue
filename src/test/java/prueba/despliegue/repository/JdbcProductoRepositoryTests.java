package prueba.despliegue.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import prueba.despliegue.model.Producto;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JdbcProductoRepositoryTests {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void productoPermaneceDisponibleParaOtraInstanciaDelRepositorio() {
        String codigo = "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        var repository = new JdbcProductoRepository(jdbc);
        var creado = repository.save(new Producto(0, codigo, "Producto de prueba", "Pruebas", new BigDecimal("19.90"), 2));

        try {
            var otraInstancia = new JdbcProductoRepository(jdbc);
            assertThat(otraInstancia.findById(creado.id())).contains(creado);

            var actualizado = new Producto(creado.id(), codigo, "Producto actualizado", "Pruebas", new BigDecimal("24.50"), 3);
            otraInstancia.save(actualizado);
            assertThat(repository.findByCodigo(codigo)).contains(actualizado);
        } finally {
            repository.deleteById(creado.id());
        }

        assertThat(repository.findById(creado.id())).isEmpty();
    }
}
