package enums;

public enum NivoSpreme {
    IV(1.0),
    V(1.2),
    VI(1.4),
    VII(1.6);

    private final double koeficijent;

    NivoSpreme(double koeficijent) {
        this.koeficijent = koeficijent;
    }

    public double getKoeficijent() {
        return koeficijent;
    }
}