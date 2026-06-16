package menadzment;

import enums.*;
import model.*;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class IzvestajMenadzerTest {
    @Test
    public void izvestajIzdavanjaBrojiIzdavanjaPoAgentu() {
        Fixture f = new Fixture();
        f.izdavanja.izdavanja.add(new Izdavanje(1, f.rezervacija, f.agent, f.vozilo,
                LocalDate.now(), LocalDate.now(), null, 1000, null));

        assertEquals(1, f.menadzer.izvestajIzdavanja(LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)).get(0).getBrojIzdavanja());
    }

    @Test
    public void izvestajRezervacijaBrojiStatuse() {
        Fixture f = new Fixture();
        f.rezervacije.rezervacije.add(f.rezervacija);
        f.rezervacije.rezervacije.add(TestSupport.rezervacija(2, f.klijent, f.model,
                StatusRezervacije.ODBIJENA, LocalDate.now(), LocalDate.now()));

        IzvestajMenadzer.RezervacijeStatistika s = f.menadzer.izvestajRezervacija(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertEquals(1, s.getPotvrdjene());
        assertEquals(1, s.getOdbijene());
    }

    @Test
    public void izvestajModelaBrojiRezervacijeIIznajmljivanja() {
        Fixture f = new Fixture();
        f.rezervacije.rezervacije.add(f.rezervacija);
        f.izdavanja.izdavanja.add(new Izdavanje(1, f.rezervacija, f.agent, f.vozilo,
                LocalDate.now(), LocalDate.now(), null, 1000, null));

        IzvestajMenadzer.ModelVozilaStatistika s = f.menadzer.izvestajModela(
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)).get(0);

        assertEquals(1, s.getBrojRezervacija());
        assertEquals(1, s.getBrojIznajmljivanja());
    }

    @Test
    public void izvestajPrihodaIRashodaSabiraPrihodeIPlate() {
        Fixture f = new Fixture();
        f.pretplate.pretplate.add(new Pretplata(1, f.klijent, LocalDate.now(), LocalDate.now().plusYears(1),
                StatusPretplate.AKTIVNA, 12000));
        f.rezervacija.setKazna(700);
        f.izdavanja.izdavanja.add(new Izdavanje(1, f.rezervacija, f.agent, f.vozilo,
                LocalDate.now(), LocalDate.now().minusDays(1), LocalDate.now(), 1000, 1100));

        IzvestajMenadzer.PrihodiRashodi r = f.menadzer.izvestajPrihodaIRashoda(LocalDate.now(),
                LocalDate.now());

        assertEquals(12000, r.getPretplate(), 0.0001);
        assertEquals(3000, r.getNajmovi(), 0.0001);
        assertEquals(200, r.getDodatneUsluge(), 0.0001);
        assertEquals(700, r.getKazne(), 0.0001);
        assertTrue(r.getRashodi() > 0);
    }

    static class Fixture {
        final Agent agent = TestSupport.agent(1);
        final Klijent klijent = TestSupport.klijent(1);
        final ModelVozila model = TestSupport.model(1, KategorijaVozila.ECONOMY);
        final Vozilo vozilo = new Vozilo(1, model, "NS-001", StatusVozila.DOSTUPNO, 1000);
        final Rezervacija rezervacija = TestSupport.rezervacija(1, klijent, model, StatusRezervacije.POTVRDJENA,
                LocalDate.now(), LocalDate.now());
        final TestSupport.Korisnici korisnici = new TestSupport.Korisnici();
        final TestSupport.Rezervacije rezervacije = new TestSupport.Rezervacije();
        final TestSupport.Izdavanja izdavanja = new TestSupport.Izdavanja();
        final TestSupport.Pretplate pretplate = new TestSupport.Pretplate();
        final TestSupport.Modeli modeli = new TestSupport.Modeli();
        final IzvestajMenadzer menadzer;

        Fixture() {
            korisnici.dodaj(agent);
            korisnici.dodaj(TestSupport.admin(1));
            modeli.modeli.add(model);
            menadzer = new IzvestajMenadzer(korisnici, rezervacije, izdavanja, pretplate, modeli);
        }
    }
}
