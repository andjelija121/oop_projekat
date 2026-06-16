package menadzment;

import enums.*;
import model.*;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class IzdavanjeMenadzerTest {
    @Test
    public void agentIzdajeVoziloZaPotvrdjenuRezervaciju() {
        Fixture f = new Fixture();

        assertTrue(f.menadzer.izdajVozilo(f.agent, 1, 1, 1200));
        assertEquals(StatusVozila.IZDATO, f.vozilo.getStatus());
        assertEquals(1, f.izdavanja.ucitajSve().size());
    }

    @Test
    public void neMozeIzdavanjeNepotvrdjeneRezervacije() {
        Fixture f = new Fixture();
        f.rezervacija.setStatus(StatusRezervacije.NA_CEKANJU);

        assertFalse(f.menadzer.izdajVozilo(f.agent, 1, 1, 1200));
    }

    @Test
    public void neMozeIzdavanjeVozilaPogresnogModela() {
        Fixture f = new Fixture();
        f.vozila.vozila.add(new Vozilo(2, TestSupport.model(2, KategorijaVozila.LUXURY),
                "BG-222", StatusVozila.DOSTUPNO, 1000));

        assertFalse(f.menadzer.izdajVozilo(f.agent, 1, 2, 1200));
    }

    @Test
    public void neMozeIzdavanjeSaManjomKilometrazom() {
        Fixture f = new Fixture();

        assertFalse(f.menadzer.izdajVozilo(f.agent, 1, 1, 900));
    }

    @Test
    public void vracanjeVozilaOslobadjaVoziloIUpisujeKaznu() {
        Fixture f = new Fixture();
        Izdavanje izdavanje = new Izdavanje(1, f.rezervacija, f.agent, f.vozilo,
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), null, 1200, null);
        f.izdavanja.izdavanja.add(izdavanje);
        f.vozilo.setStatus(StatusVozila.IZDATO);

        assertTrue(f.menadzer.vratiVozilo(f.agent, 1, LocalDate.now(), 1500));
        assertEquals(StatusVozila.DOSTUPNO, f.vozilo.getStatus());
        assertEquals(700, f.rezervacija.getKazna(), 0.0001);
    }

    @Test
    public void ucitavajuSeSamoDostupnaVozilaZaRezervaciju() {
        Fixture f = new Fixture();
        f.vozila.vozila.add(new Vozilo(2, f.model, "NS-002", StatusVozila.IZDATO, 900));

        assertEquals(1, f.menadzer.ucitajDostupnaVozilaZaRezervaciju(1).size());
    }

    @Test
    public void opisIzdavanjaRazlikujeNeizdatoIzdatoIVraceno() {
        Fixture f = new Fixture();
        assertEquals("Nije izdato", f.menadzer.opisIzdavanjaRezervacije(1));

        Izdavanje izdavanje = new Izdavanje(1, f.rezervacija, f.agent, f.vozilo,
                LocalDate.now(), LocalDate.now(), null, 1200, null);
        f.izdavanja.izdavanja.add(izdavanje);
        assertEquals("Izdato", f.menadzer.opisIzdavanjaRezervacije(1));

        izdavanje.setDatumVracanjaStvarno(LocalDate.now());
        assertEquals("Vraceno", f.menadzer.opisIzdavanjaRezervacije(1));
    }

    static class Fixture {
        final Agent agent = TestSupport.agent(1);
        final Klijent klijent = TestSupport.klijent(1);
        final ModelVozila model = TestSupport.model(1, KategorijaVozila.ECONOMY);
        final Vozilo vozilo = new Vozilo(1, model, "NS-001", StatusVozila.DOSTUPNO, 1000);
        final Rezervacija rezervacija = TestSupport.rezervacija(1, klijent, model, StatusRezervacije.POTVRDJENA,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        final TestSupport.Izdavanja izdavanja = new TestSupport.Izdavanja();
        final TestSupport.Rezervacije rezervacije = new TestSupport.Rezervacije();
        final TestSupport.Vozila vozila = new TestSupport.Vozila();
        final IzdavanjeMenadzer menadzer;

        Fixture() {
            rezervacije.rezervacije.add(rezervacija);
            vozila.vozila.add(vozilo);
            menadzer = new IzdavanjeMenadzer(izdavanja, rezervacije, vozila, CenovnikMenadzerTest.menadzerSaCenovnikom());
        }
    }
}
