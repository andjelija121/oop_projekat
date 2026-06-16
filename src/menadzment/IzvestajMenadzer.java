package menadzment;

import enums.KategorijaKlijenta;
import enums.StatusRezervacije;
import model.Agent;
import model.Izdavanje;
import model.Klijent;
import model.ModelVozila;
import model.Pretplata;
import model.Rezervacija;
import model.Zaposleni;
import repozitorijum.IzdavanjeRepozitorijum;
import repozitorijum.KorisnikRepozitorijum;
import repozitorijum.ModelVozilaRepozitorijum;
import repozitorijum.PretplataRepozitorijum;
import repozitorijum.RezervacijaRepozitorijum;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class IzvestajMenadzer {
    private final KorisnikRepozitorijum korisnikRepozitorijum;
    private final RezervacijaRepozitorijum rezervacijaRepozitorijum;
    private final IzdavanjeRepozitorijum izdavanjeRepozitorijum;
    private final PretplataRepozitorijum pretplataRepozitorijum;
    private final ModelVozilaRepozitorijum modelVozilaRepozitorijum;

    public IzvestajMenadzer(KorisnikRepozitorijum korisnikRepozitorijum,
                            RezervacijaRepozitorijum rezervacijaRepozitorijum,
                            IzdavanjeRepozitorijum izdavanjeRepozitorijum,
                            PretplataRepozitorijum pretplataRepozitorijum,
                            ModelVozilaRepozitorijum modelVozilaRepozitorijum) {
        this.korisnikRepozitorijum = korisnikRepozitorijum;
        this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
        this.izdavanjeRepozitorijum = izdavanjeRepozitorijum;
        this.pretplataRepozitorijum = pretplataRepozitorijum;
        this.modelVozilaRepozitorijum = modelVozilaRepozitorijum;
    }

    public ArrayList<IzdavanjeAgenta> izvestajIzdavanja(LocalDate datumOd, LocalDate datumDo) {
        ArrayList<IzdavanjeAgenta> rezultat = new ArrayList<>();

        for (Agent agent : korisnikRepozitorijum.ucitajAgente()) {
            int broj = 0;
            for (Izdavanje izdavanje : izdavanjeRepozitorijum.ucitajSve()) {
                if (istiAgent(agent, izdavanje.getAgent())
                        && uOpsegu(izdavanje.getDatumIzdavanja(), datumOd, datumDo)) {
                    broj++;
                }
            }
            rezultat.add(new IzdavanjeAgenta(agent, broj));
        }

        return rezultat;
    }

    public RezervacijeStatistika izvestajRezervacija(LocalDate datumOd, LocalDate datumDo) {
        RezervacijeStatistika rezultat = new RezervacijeStatistika();

        for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
            if (rezervacijaPresecaPeriod(rezervacija, datumOd, datumDo)) {
                rezultat.dodaj(rezervacija.getStatus());
            }
        }

        return rezultat;
    }

    public ArrayList<ModelVozilaStatistika> izvestajModela(LocalDate datumOd, LocalDate datumDo) {
        ArrayList<ModelVozilaStatistika> rezultat = new ArrayList<>();

        for (ModelVozila model : modelVozilaRepozitorijum.ucitajSve()) {
            int brojRezervacija = 0;
            int brojIznajmljivanja = 0;

            for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
                if (rezervacija.odnosiSeNaModel(model) && rezervacijaPresecaPeriod(rezervacija, datumOd, datumDo)) {
                    brojRezervacija++;
                }
            }

            for (Izdavanje izdavanje : izdavanjeRepozitorijum.ucitajSve()) {
                Rezervacija rezervacija = izdavanje.getRezervacija();
                if (rezervacija != null && rezervacija.odnosiSeNaModel(model)
                        && uOpsegu(izdavanje.getDatumIzdavanja(), datumOd, datumDo)) {
                    brojIznajmljivanja++;
                }
            }

            rezultat.add(new ModelVozilaStatistika(model, brojIznajmljivanja, brojRezervacija));
        }

        return rezultat;
    }

    public PrihodiRashodi izvestajPrihodaIRashoda(LocalDate datumOd, LocalDate datumDo) {
        Prihod prihod = izracunajPrihod(datumOd, datumDo, null);
        double rashodi = izracunajPlate(datumOd, datumDo);
        return new PrihodiRashodi(prihod.pretplate, prihod.najmovi, prihod.dodatneUsluge,
                prihod.kazne, rashodi);
    }

    public LinkedHashMap<YearMonth, LinkedHashMap<KategorijaKlijenta, Double>> prihodiPoMesecimaIKategoriji() {
        LinkedHashMap<YearMonth, LinkedHashMap<KategorijaKlijenta, Double>> rezultat = new LinkedHashMap<>();
        YearMonth pocetniMesec = YearMonth.from(LocalDate.now()).minusMonths(11);

        for (int i = 0; i < 12; i++) {
            YearMonth mesec = pocetniMesec.plusMonths(i);
            LinkedHashMap<KategorijaKlijenta, Double> poKategoriji = new LinkedHashMap<>();
            for (KategorijaKlijenta kategorija : KategorijaKlijenta.values()) {
                poKategoriji.put(kategorija, 0.0);
            }
            rezultat.put(mesec, poKategoriji);
        }

        LocalDate datumOd = pocetniMesec.atDay(1);
        LocalDate datumDo = YearMonth.from(LocalDate.now()).atEndOfMonth();
        dodajPrihodePoKategorijama(rezultat, datumOd, datumDo);
        return rezultat;
    }

    public LinkedHashMap<Agent, Integer> opterecenjeAgenataZaPrethodnih30Dana() {
        LinkedHashMap<Agent, Integer> rezultat = new LinkedHashMap<>();
        LocalDate datumOd = LocalDate.now().minusDays(29);
        LocalDate datumDo = LocalDate.now();

        for (Agent agent : korisnikRepozitorijum.ucitajAgente()) {
            rezultat.put(agent, 0);
        }

        for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
            Agent agent = rezervacija.getAgentObrade();
            Agent agentIzMape = pronadjiAgentaUKljucu(rezultat, agent);
            if (agentIzMape != null
                    && uOpsegu(rezervacija.getDatumObrade(), datumOd, datumDo)
                    && (rezervacija.getStatus() == StatusRezervacije.POTVRDJENA
                    || rezervacija.getStatus() == StatusRezervacije.ODBIJENA)) {
                rezultat.put(agentIzMape, rezultat.get(agentIzMape) + 1);
            }
        }

        return rezultat;
    }

    public RezervacijeStatistika statusiRezervacijaKreiranihZaPrethodnih30Dana() {
        RezervacijeStatistika rezultat = new RezervacijeStatistika();
        LocalDate datumOd = LocalDate.now().minusDays(29);
        LocalDate datumDo = LocalDate.now();

        for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
            if (uOpsegu(rezervacija.getDatumKreiranja(), datumOd, datumDo)) {
                rezultat.dodaj(rezervacija.getStatus());
            }
        }

        return rezultat;
    }

    private void dodajPrihodePoKategorijama(LinkedHashMap<YearMonth, LinkedHashMap<KategorijaKlijenta, Double>> rezultat,
                                            LocalDate datumOd, LocalDate datumDo) {
        for (Pretplata pretplata : pretplataRepozitorijum.ucitajSve()) {
            if (uOpsegu(pretplata.getDatumPocetka(), datumOd, datumDo)) {
                dodajPrihod(rezultat, pretplata.getDatumPocetka(), kategorijaKlijenta(pretplata.getKlijent()),
                        pretplata.getCena());
            }
        }

        for (Izdavanje izdavanje : izdavanjeRepozitorijum.ucitajSve()) {
            Rezervacija rezervacija = izdavanje.getRezervacija();
            if (rezervacija == null) {
                continue;
            }

            KategorijaKlijenta kategorija = kategorijaKlijenta(rezervacija.getKlijent());
            if (uOpsegu(izdavanje.getDatumIzdavanja(), datumOd, datumDo)) {
                dodajPrihod(rezultat, izdavanje.getDatumIzdavanja(), kategorija,
                        rezervacija.getCenaNajma() + rezervacija.getCenaDodatnihUsluga());
            }

            if (uOpsegu(izdavanje.getDatumVracanjaStvarno(), datumOd, datumDo)) {
                dodajPrihod(rezultat, izdavanje.getDatumVracanjaStvarno(), kategorija, rezervacija.getKazna());
            }
        }
    }

    private void dodajPrihod(LinkedHashMap<YearMonth, LinkedHashMap<KategorijaKlijenta, Double>> rezultat,
                             LocalDate datum, KategorijaKlijenta kategorija, double iznos) {
        YearMonth mesec = YearMonth.from(datum);
        LinkedHashMap<KategorijaKlijenta, Double> poKategoriji = rezultat.get(mesec);

        if (poKategoriji != null) {
            poKategoriji.put(kategorija, poKategoriji.get(kategorija) + iznos);
        }
    }

    private Prihod izracunajPrihod(LocalDate datumOd, LocalDate datumDo, KategorijaKlijenta kategorijaFilter) {
        Prihod prihod = new Prihod();

        for (Pretplata pretplata : pretplataRepozitorijum.ucitajSve()) {
            if (uOpsegu(pretplata.getDatumPocetka(), datumOd, datumDo)
                    && poklapaKategoriju(pretplata.getKlijent(), kategorijaFilter)) {
                prihod.pretplate += pretplata.getCena();
            }
        }

        for (Izdavanje izdavanje : izdavanjeRepozitorijum.ucitajSve()) {
            Rezervacija rezervacija = izdavanje.getRezervacija();
            if (rezervacija == null || !poklapaKategoriju(rezervacija.getKlijent(), kategorijaFilter)) {
                continue;
            }

            if (uOpsegu(izdavanje.getDatumIzdavanja(), datumOd, datumDo)) {
                prihod.najmovi += rezervacija.getCenaNajma();
                prihod.dodatneUsluge += rezervacija.getCenaDodatnihUsluga();
            }

            if (uOpsegu(izdavanje.getDatumVracanjaStvarno(), datumOd, datumDo)) {
                prihod.kazne += rezervacija.getKazna();
            }
        }

        return prihod;
    }

    private double izracunajPlate(LocalDate datumOd, LocalDate datumDo) {
        long brojMeseci = ChronoUnit.MONTHS.between(
                YearMonth.from(datumOd).atDay(1),
                YearMonth.from(datumDo).atDay(1)) + 1;
        double mesecnePlate = 0;

        for (Zaposleni zaposleni : sviZaposleni()) {
            mesecnePlate += zaposleni.getPlata();
        }

        return mesecnePlate * brojMeseci;
    }

    private ArrayList<Zaposleni> sviZaposleni() {
        ArrayList<Zaposleni> rezultat = new ArrayList<>();
        rezultat.addAll(korisnikRepozitorijum.ucitajAdministratore());
        rezultat.addAll(korisnikRepozitorijum.ucitajAgente());
        return rezultat;
    }

    private boolean rezervacijaPresecaPeriod(Rezervacija rezervacija, LocalDate datumOd, LocalDate datumDo) {
        return rezervacija.getDatumOd() != null && rezervacija.getDatumDo() != null
                && !rezervacija.getDatumDo().isBefore(datumOd)
                && !rezervacija.getDatumOd().isAfter(datumDo);
    }

    private boolean uOpsegu(LocalDate datum, LocalDate datumOd, LocalDate datumDo) {
        return datum != null && !datum.isBefore(datumOd) && !datum.isAfter(datumDo);
    }

    private boolean istiAgent(Agent prvi, Agent drugi) {
        return prvi != null && drugi != null && prvi.getId() == drugi.getId();
    }

    private Agent pronadjiAgentaUKljucu(LinkedHashMap<Agent, Integer> rezultat, Agent trazeniAgent) {
        for (Agent agent : rezultat.keySet()) {
            if (istiAgent(agent, trazeniAgent)) {
                return agent;
            }
        }

        return null;
    }

    private KategorijaKlijenta kategorijaKlijenta(Klijent klijent) {
        return klijent == null || klijent.getPosebnaKategorija() == null
                ? KategorijaKlijenta.BEZ_KATEGORIJE
                : klijent.getPosebnaKategorija();
    }

    private boolean poklapaKategoriju(Klijent klijent, KategorijaKlijenta kategorijaFilter) {
        return kategorijaFilter == null || kategorijaKlijenta(klijent) == kategorijaFilter;
    }

    private static class Prihod {
        private double pretplate;
        private double najmovi;
        private double dodatneUsluge;
        private double kazne;
    }

    public static class IzdavanjeAgenta {
        private final Agent agent;
        private final int brojIzdavanja;

        public IzdavanjeAgenta(Agent agent, int brojIzdavanja) {
            this.agent = agent;
            this.brojIzdavanja = brojIzdavanja;
        }

        public Agent getAgent() {
            return agent;
        }

        public int getBrojIzdavanja() {
            return brojIzdavanja;
        }
    }

    public static class ModelVozilaStatistika {
        private final ModelVozila model;
        private final int brojIznajmljivanja;
        private final int brojRezervacija;

        public ModelVozilaStatistika(ModelVozila model, int brojIznajmljivanja, int brojRezervacija) {
            this.model = model;
            this.brojIznajmljivanja = brojIznajmljivanja;
            this.brojRezervacija = brojRezervacija;
        }

        public ModelVozila getModel() {
            return model;
        }

        public int getBrojIznajmljivanja() {
            return brojIznajmljivanja;
        }

        public int getBrojRezervacija() {
            return brojRezervacija;
        }
    }

    public static class PrihodiRashodi {
        private final double pretplate;
        private final double najmovi;
        private final double dodatneUsluge;
        private final double kazne;
        private final double rashodi;

        public PrihodiRashodi(double pretplate, double najmovi, double dodatneUsluge,
                              double kazne, double rashodi) {
            this.pretplate = pretplate;
            this.najmovi = najmovi;
            this.dodatneUsluge = dodatneUsluge;
            this.kazne = kazne;
            this.rashodi = rashodi;
        }

        public double getPretplate() {
            return pretplate;
        }

        public double getNajmovi() {
            return najmovi;
        }

        public double getDodatneUsluge() {
            return dodatneUsluge;
        }

        public double getKazne() {
            return kazne;
        }

        public double getPrihodiUkupno() {
            return pretplate + najmovi + dodatneUsluge + kazne;
        }

        public double getRashodi() {
            return rashodi;
        }

        public double getProfit() {
            return getPrihodiUkupno() - rashodi;
        }
    }

    public static class RezervacijeStatistika {
        private int naCekanju;
        private int potvrdjene;
        private int odbijene;
        private int otkazane;
        private int zavrsene;

        public void dodaj(StatusRezervacije status) {
            if (status == StatusRezervacije.NA_CEKANJU) {
                naCekanju++;
            } else if (status == StatusRezervacije.POTVRDJENA) {
                potvrdjene++;
            } else if (status == StatusRezervacije.ODBIJENA) {
                odbijene++;
            } else if (status == StatusRezervacije.OTKAZANA) {
                otkazane++;
            } else if (status == StatusRezervacije.ZAVRSENA) {
                zavrsene++;
            }
        }

        public int getNaCekanju() {
            return naCekanju;
        }

        public int getPotvrdjene() {
            return potvrdjene;
        }

        public int getOdbijene() {
            return odbijene;
        }

        public int getOtkazane() {
            return otkazane;
        }

        public int getZavrsene() {
            return zavrsene;
        }
    }
}
