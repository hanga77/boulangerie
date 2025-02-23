package lab.hang.Gestion.boulangerie.exception;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String message) {
        super(message);
    }
}
