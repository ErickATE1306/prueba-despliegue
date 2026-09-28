package prueba.despliegue.exception;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(long id) {
        super("No existe un producto con ID " + id);
    }
}
