package prueba.despliegue.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import prueba.despliegue.dto.ProductoRequest;
import prueba.despliegue.service.ProductoService;

import java.math.BigDecimal;

@Configuration
public class DatosDemoConfig {
    @Bean
    ApplicationRunner cargarProductosDemo(ProductoService service) {
        return args -> {
            service.crear(new ProductoRequest("LAP-001", "Laptop Pro 14", "Tecnología", new BigDecimal("3499.00"), 8));
            service.crear(new ProductoRequest("MON-002", "Monitor 27 pulgadas", "Tecnología", new BigDecimal("899.90"), 3));
            service.crear(new ProductoRequest("SIL-003", "Silla ergonómica", "Oficina", new BigDecimal("649.00"), 12));
            service.crear(new ProductoRequest("TEC-004", "Teclado mecánico", "Accesorios", new BigDecimal("289.00"), 0));
        };
    }
}
