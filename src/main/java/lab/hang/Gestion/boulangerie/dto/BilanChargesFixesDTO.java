package lab.hang.Gestion.boulangerie.dto;

public class BilanChargesFixesDTO {

    private int nombreChargesEnRetard;
    private double montantTotalEnRetard;
    private double montantTotalAVenir;
    private double montantTotalPaye;

    public int getNombreChargesEnRetard() {
        return nombreChargesEnRetard;
    }

    public void setNombreChargesEnRetard(int nombreChargesEnRetard) {
        this.nombreChargesEnRetard = nombreChargesEnRetard;
    }

    public double getMontantTotalEnRetard() {
        return montantTotalEnRetard;
    }

    public void setMontantTotalEnRetard(double montantTotalEnRetard) {
        this.montantTotalEnRetard = montantTotalEnRetard;
    }

    public double getMontantTotalAVenir() {
        return montantTotalAVenir;
    }

    public void setMontantTotalAVenir(double montantTotalAVenir) {
        this.montantTotalAVenir = montantTotalAVenir;
    }

    public double getMontantTotalPaye() {
        return montantTotalPaye;
    }

    public void setMontantTotalPaye(double montantTotalPaye) {
        this.montantTotalPaye = montantTotalPaye;
    }
}
