package model;

public class RezervacijaUsluga {
    private int id;
    private int rezervacijaId;
    private DodatnaUsluga dodatnaUsluga;
    private int kolicina;
    private double cenaPoJedinici;
    private double ukupno;

    public RezervacijaUsluga(int id, int rezervacijaId, DodatnaUsluga dodatnaUsluga,
                             int kolicina, double cenaPoJedinici, double ukupno) {
        this.id = id;
        this.rezervacijaId = rezervacijaId;
        this.dodatnaUsluga = dodatnaUsluga;
        this.kolicina = kolicina;
        this.cenaPoJedinici = cenaPoJedinici;
        this.ukupno = ukupno;
    }

    public int getId() {
        return id;
    }

    public int getRezervacijaId() {
        return rezervacijaId;
    }

    public DodatnaUsluga getDodatnaUsluga() {
        return dodatnaUsluga;
    }

    public int getKolicina() {
        return kolicina;
    }

    public double getCenaPoJedinici() {
        return cenaPoJedinici;
    }

    public double getUkupno() {
        return ukupno;
    }
}
