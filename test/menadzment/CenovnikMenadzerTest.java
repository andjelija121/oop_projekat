package menadzment;

import enums.*;
import model.Cenovnik;
import model.DodatnaUsluga;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class CenovnikMenadzerTest {
    @Test
    public void izracunajCenuNajmaZaObicnogKlijenta() {
        double cena = menadzerSaCenovnikom().izracunajCenuNajma(TestSupport.klijent(1),
                TestSupport.model(1, KategorijaVozila.ECONOMY),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 0);

        assertEquals(3000, cena, 0.0001);
    }

    @Test
    public void izracunajCenuNajmaUvazavaPopustKlijenta() {
        double cena = menadzerSaCenovnikom().izracunajCenuNajma(TestSupport.student(1),
                TestSupport.model(1, KategorijaVozila.ECONOMY),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 0);

        assertEquals(2400, cena, 0.0001);
    }

    @Test
    public void izracunajCenuDodatnihUslugaSabiraUslugeIProduzenje() {
        ArrayList<DodatnaUsluga> usluge = new ArrayList<>();
        usluge.add(TestSupport.gps());

        double cena = menadzerSaCenovnikom().izracunajCenuDodatnihUsluga(
                LocalDate.now().plusDays(1), usluge, 1);

        assertEquals(800, cena, 0.0001);
    }

    @Test
    public void izracunajKaznuKasnjenjaRacunaPoDanu() {
        double kazna = menadzerSaCenovnikom().izracunajKaznuKasnjenja(
                LocalDate.now().minusDays(2), LocalDate.now());

        assertEquals(1400, kazna, 0.0001);
    }

    @Test
    public void administratorMozeDaDodaNoviCenovnik() {
        TestSupport.Cenovnici repo = new TestSupport.Cenovnici();
        repo.cenovnici.add(TestSupport.cenovnik(LocalDate.now().minusDays(10), LocalDate.now().plusDays(10)));
        CenovnikMenadzer menadzer = new CenovnikMenadzer(repo);
        TestSupport.setField(menadzer, "podesavanjaRepozitorijum", new TestSupport.Podesavanja());

        boolean dodat = menadzer.dodajNoviCenovnik(TestSupport.admin(1), LocalDate.now().plusDays(11),
                LocalDate.now().plusDays(30), 15000, ceneNajma(), popusti(), ceneUsluga(), 900);

        assertTrue(dodat);
        assertEquals(2, repo.ucitajSve().size());
    }

    static CenovnikMenadzer menadzerSaCenovnikom() {
        TestSupport.Cenovnici repo = new TestSupport.Cenovnici();
        repo.cenovnici.add(TestSupport.cenovnik(LocalDate.now().minusYears(1), LocalDate.now().plusYears(1)));
        CenovnikMenadzer menadzer = new CenovnikMenadzer(repo);
        TestSupport.setField(menadzer, "podesavanjaRepozitorijum", new TestSupport.Podesavanja());
        return menadzer;
    }

    static HashMap<KategorijaVozila, Double> ceneNajma() {
        HashMap<KategorijaVozila, Double> cene = new HashMap<>();
        for (KategorijaVozila kategorija : KategorijaVozila.values()) {
            cene.put(kategorija, 1000.0);
        }
        return cene;
    }

    static HashMap<KategorijaKlijenta, Double> popusti() {
        HashMap<KategorijaKlijenta, Double> popusti = new HashMap<>();
        for (KategorijaKlijenta kategorija : KategorijaKlijenta.values()) {
            popusti.put(kategorija, 0.0);
        }
        return popusti;
    }

    static HashMap<DodatnaUsluga, Double> ceneUsluga() {
        HashMap<DodatnaUsluga, Double> cene = new HashMap<>();
        cene.put(TestSupport.gps(), 300.0);
        return cene;
    }
}
