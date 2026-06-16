package menadzment;

import enums.*;
import model.*;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class RezervacijaMenadzerTest {
    @Test
    public void modelJeDostupanKadaPostojiSlobodnoVozilo() {
        Fixture f = new Fixture();

        assertTrue(f.menadzer.daLiJeModelDostupan(f.model,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)));
    }

    @Test
    public void modelNijeDostupanKadaSuSviPrimerciZauzeti() {
        Fixture f = new Fixture();
        f.rezervacije.rezervacije.add(TestSupport.rezervacija(1, f.klijent, f.model,
                StatusRezervacije.POTVRDJENA, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)));

        assertFalse(f.menadzer.daLiJeModelDostupan(f.model,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)));
    }

    @Test
    public void validanZahtevZaRezervacijuSeCuva() {
        Fixture f = new Fixture();

        Rezervacija zahtev = f.menadzer.napraviZahtevZaRezervaciju(f.klijent, f.model,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 3000, 0, 3000);

        assertNotNull(zahtev);
        assertEquals(StatusRezervacije.NA_CEKANJU, zahtev.getStatus());
        assertEquals(1, f.rezervacije.ucitajSve().size());
    }

    @Test
    public void zahtevSeOdbijaZaNevazecuDozvolu() {
        Fixture f = new Fixture();
        Klijent noviVozac = new Klijent(2, "Novi", "Vozac", Pol.MUSKI, "2000-01-01", "060", "A",
                "novi", "pass", LocalDate.now().minusMonths(6).toString(), KategorijaKlijenta.BEZ_KATEGORIJE);

        assertNull(f.menadzer.napraviZahtevZaRezervaciju(noviVozac, f.model,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 3000, 0, 3000));
    }

    @Test
    public void agentPotvrdjujeRezervacijuNaCekanju() {
        Fixture f = new Fixture();
        Rezervacija rezervacija = TestSupport.rezervacija(1, f.klijent, f.model, StatusRezervacije.NA_CEKANJU,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        f.rezervacije.rezervacije.add(rezervacija);

        assertTrue(f.menadzer.potvrdiRezervaciju(f.agent, 1));
        assertEquals(StatusRezervacije.POTVRDJENA, rezervacija.getStatus());
    }

    @Test
    public void agentOdbijaRezervacijuNaCekanju() {
        Fixture f = new Fixture();
        Rezervacija rezervacija = TestSupport.rezervacija(1, f.klijent, f.model, StatusRezervacije.NA_CEKANJU,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        f.rezervacije.rezervacije.add(rezervacija);

        assertTrue(f.menadzer.odbijRezervaciju(f.agent, 1));
        assertEquals(StatusRezervacije.ODBIJENA, rezervacija.getStatus());
    }

    @Test
    public void klijentMozeDaOtkazeSvojuRezervaciju() {
        Fixture f = new Fixture();
        Rezervacija rezervacija = TestSupport.rezervacija(1, f.klijent, f.model, StatusRezervacije.POTVRDJENA,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        f.rezervacije.rezervacije.add(rezervacija);

        assertTrue(f.menadzer.otkaziRezervaciju(f.klijent, 1));
        assertEquals(StatusRezervacije.OTKAZANA, rezervacija.getStatus());
    }

    @Test
    public void klijentNeMozeDaOtkazeTudjuRezervaciju() {
        Fixture f = new Fixture();
        f.rezervacije.rezervacije.add(TestSupport.rezervacija(1, f.klijent, f.model,
                StatusRezervacije.POTVRDJENA, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)));

        assertFalse(f.menadzer.otkaziRezervaciju(TestSupport.klijent(9), 1));
    }

    @Test
    public void dodatnaUslugaPovecavaCenuPotvrdjeneRezervacije() {
        Fixture f = new Fixture();
        Rezervacija rezervacija = TestSupport.rezervacija(1, f.klijent, f.model, StatusRezervacije.POTVRDJENA,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        f.rezervacije.rezervacije.add(rezervacija);

        assertTrue(f.menadzer.dodajDodatnuUsluguNaRezervaciju(f.agent, 1, TestSupport.gps(), 2, 300));
        assertEquals(800, rezervacija.getCenaDodatnihUsluga(), 0.0001);
        assertEquals(3800, rezervacija.getCenaUkupno(), 0.0001);
    }

    @Test
    public void filtriranjeModelaRadiPoNazivuProizvodjacuIKategoriji() {
        Fixture f = new Fixture();
        f.modeli.modeli.add(new ModelVozila(2, "Clio", "Renault", KategorijaVozila.ECONOMY));

        assertEquals(1, f.menadzer.filtrirajModele("golf", "vw", KategorijaVozila.ECONOMY).size());
    }

    @Test
    public void zabranaRezervisanjaVaziDvadesetCetiriSataNakonOtkazivanja() {
        Fixture f = new Fixture();
        f.rezervacije.rezervacije.add(new Rezervacija(1, f.klijent, f.model, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3), StatusRezervacije.OTKAZANA, 0, 0, 0, 0,
                LocalDateTime.now().minusHours(2)));

        assertTrue(f.menadzer.klijentImaZabranuRezervisanja(f.klijent));
    }

    static class Fixture {
        final TestSupport.Rezervacije rezervacije = new TestSupport.Rezervacije();
        final TestSupport.Vozila vozila = new TestSupport.Vozila();
        final TestSupport.Modeli modeli = new TestSupport.Modeli();
        final TestSupport.RezervacijaUsluge usluge = new TestSupport.RezervacijaUsluge();
        final TestSupport.Izdavanja izdavanja = new TestSupport.Izdavanja();
        final ModelVozila model = TestSupport.model(1, KategorijaVozila.ECONOMY);
        final Klijent klijent = TestSupport.klijent(1);
        final Agent agent = TestSupport.agent(1);
        final RezervacijaMenadzer menadzer = new RezervacijaMenadzer(rezervacije, vozila, modeli, usluge);

        Fixture() {
            modeli.modeli.add(model);
            vozila.vozila.add(new Vozilo(1, model, "NS-001", StatusVozila.DOSTUPNO, 1000));
            menadzer.setIzdavanjeRepozitorijum(izdavanja);
        }
    }
}
