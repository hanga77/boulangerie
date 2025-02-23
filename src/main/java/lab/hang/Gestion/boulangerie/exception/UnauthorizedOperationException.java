package lab.hang.Gestion.boulangerie.exception;

public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
