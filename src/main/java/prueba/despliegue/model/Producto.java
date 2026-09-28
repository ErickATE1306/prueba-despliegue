package prueba.despliegue.model;

import java.math.BigDecimal;

public record Producto(
        long id,
        String codigo,
        String nombre,
        String categoria,
        BigDecimal precio,
        int stock
) {
}
