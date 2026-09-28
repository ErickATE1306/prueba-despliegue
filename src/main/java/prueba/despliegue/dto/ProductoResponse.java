package prueba.despliegue.dto;

import prueba.despliegue.model.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        long id,
        String codigo,
        String nombre,
        String categoria,
        BigDecimal precio,
        int stock
) {
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.id(),
                producto.codigo(),
                producto.nombre(),
                producto.categoria(),
                producto.precio(),
                producto.stock()
        );
    }
}
