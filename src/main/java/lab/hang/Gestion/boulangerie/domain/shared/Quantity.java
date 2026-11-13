package lab.hang.Gestion.boulangerie.domain.shared;

import java.util.Objects;

/**
 * Quantité strictement positive ou nulle selon le contexte.
 */
public final class Quantity {

    private final double value;

    private Quantity(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Quantité invalide: " + value);
        }
        this.value = value;
    }

    public static Quantity of(double value) {
        return new Quantity(value);
    }

    public static Quantity zero() {
        return new Quantity(0);
    }

    public double value() {
        return value;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        return new Quantity(this.value - other.value);
    }

    public boolean isPositive() {
        return value > 0;
    }

    public boolean isZero() {
        return value == 0;
    }

    public void requirePositive(String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message != null ? message : "La quantité doit être positive");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity quantity)) return false;
        return Double.compare(quantity.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
