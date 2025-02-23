package lab.hang.Gestion.boulangerie.exception;

public class UserNotAuthenticatedException extends RuntimeException {
    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}
