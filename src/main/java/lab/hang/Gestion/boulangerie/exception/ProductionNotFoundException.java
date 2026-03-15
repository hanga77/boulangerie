package lab.hang.Gestion.boulangerie.exception;

public class ProductionNotFoundException extends RuntimeException {
    public ProductionNotFoundException(String message) {
        super(message);
    }
}
