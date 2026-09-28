package prueba.despliegue.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import prueba.despliegue.dto.ProductoRequest;
import prueba.despliegue.exception.CodigoDuplicadoException;
import prueba.despliegue.exception.ProductoNoEncontradoException;
import prueba.despliegue.repository.InMemoryProductoRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoServiceTests {
    private ProductoService service;

    @BeforeEach
    void configurar() {
        service = new ProductoService(new InMemoryProductoRepository());
    }

    @Test
    void creaYBuscaProductoConCodigoNormalizado() {
        var creado = service.crear(request(" lap-01 ", "Laptop", 5));

        assertThat(creado.id()).isPositive();
        assertThat(creado.codigo()).isEqualTo("LAP-01");
        assertThat(service.listar("lap")).containsExactly(creado);
        assertThat(service.obtener(creado.id())).isEqualTo(creado);
    }

    @Test
    void actualizaProductoSinCambiarSuId() {
        var creado = service.crear(request("LAP-01", "Laptop", 5));

        var actualizado = service.actualizar(creado.id(), request("LAP-02", "Laptop mejorada", 2));

        assertThat(actualizado.id()).isEqualTo(creado.id());
        assertThat(actualizado.nombre()).isEqualTo("Laptop mejorada");
        assertThat(actualizado.stock()).isEqualTo(2);
        assertThat(service.listar("LAP-01")).isEmpty();
    }

    @Test
    void rechazaCodigoDuplicadoSinDistinguirMayusculas() {
        service.crear(request("LAP-01", "Laptop", 5));

        assertThatThrownBy(() -> service.crear(request("lap-01", "Otra laptop", 3)))
                .isInstanceOf(CodigoDuplicadoException.class);
    }

    @Test
    void eliminaProductoYReportaAusencias() {
        var creado = service.crear(request("LAP-01", "Laptop", 5));

        service.eliminar(creado.id());

        assertThat(service.listar(null)).isEmpty();
        assertThatThrownBy(() -> service.obtener(creado.id()))
                .isInstanceOf(ProductoNoEncontradoException.class);
    }

    private static ProductoRequest request(String codigo, String nombre, int stock) {
        return new ProductoRequest(codigo, nombre, "Tecnología", new BigDecimal("120.00"), stock);
    }
}
