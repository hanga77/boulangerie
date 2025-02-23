package lab.hang.Gestion.boulangerie.exception;

public class CommandeNotFoundException extends RuntimeException {
    public CommandeNotFoundException(String message) {
        super(message);
    }
}