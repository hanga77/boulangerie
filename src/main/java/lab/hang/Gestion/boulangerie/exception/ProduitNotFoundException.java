package lab.hang.Gestion.boulangerie.exception;

public class ProduitNotFoundException extends RuntimeException {
    public ProduitNotFoundException(String message) {
        super(message);
    }
}