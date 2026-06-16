package menadzment;

import enums.*;
import model.*;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class PretplataMenadzerTest {
    @Test
    public void praviZahtevKadaKlijentNemaAktivnuPretplatu() {
        Fixture f = new Fixture();

        ZahtevPretplate zahtev = f.menadzer.napraviZahtev(f.klijent);

        assertNotNull(zahtev);
        assertEquals(StatusPretplate.NA_CEKANJU, zahtev.getStatus());
    }

    @Test
    public void nePraviDupliZahtevNaCekanju() {
        Fixture f = new Fixture();
        f.zahtevi.zahtevi.add(new ZahtevPretplate(1, f.klijent, null, LocalDate.now(),
                StatusPretplate.NA_CEKANJU));

        assertNull(f.menadzer.napraviZahtev(f.klijent));
    }

    @Test
    public void agentOdobravaZahtevIKreiraPretplatu() {
        Fixture f = new Fixture();
        f.zahtevi.zahtevi.add(new ZahtevPretplate(1, f.klijent, null, LocalDate.now(),
                StatusPretplate.NA_CEKANJU));

        assertTrue(f.menadzer.odobriZahtev(f.agent, 1));
        assertEquals(StatusPretplate.ODOBREN, f.zahtevi.zahtevi.get(0).getStatus());
        assertEquals(1, f.pretplate.ucitajSve().size());
    }

    @Test
    public void agentOdbijaZahtev() {
        Fixture f = new Fixture();
        f.zahtevi.zahtevi.add(new ZahtevPretplate(1, f.klijent, null, LocalDate.now(),
                StatusPretplate.NA_CEKANJU));

        assertTrue(f.menadzer.odbijZahtev(f.agent, 1));
        assertEquals(StatusPretplate.ODBIJENA, f.zahtevi.zahtevi.get(0).getStatus());
    }

    @Test
    public void zahtevSeOdbijaKadaKlijentImaViseOdPetKasnjenja() {
        Fixture f = new Fixture();
        f.zahtevi.zahtevi.add(new ZahtevPretplate(1, f.klijent, null, LocalDate.now(),
                StatusPretplate.NA_CEKANJU));
        for (int i = 0; i < 6; i++) {
            Rezervacija r = TestSupport.rezervacija(i + 1, f.klijent, TestSupport.model(1, KategorijaVozila.ECONOMY),
                    StatusRezervacije.POTVRDJENA, LocalDate.now().minusDays(10), LocalDate.now().minusDays(8));
            f.izdavanja.izdavanja.add(new Izdavanje(i + 1, r, f.agent, null, LocalDate.now().minusDays(10),
                    LocalDate.now().minusDays(8), LocalDate.now().minusDays(7), 0, 0));
        }

        assertFalse(f.menadzer.odobriZahtev(f.agent, 1));
        assertEquals(StatusPretplate.ODBIJENA, f.zahtevi.zahtevi.get(0).getStatus());
    }

    static class Fixture {
        final Klijent klijent = TestSupport.klijent(1);
        final Agent agent = TestSupport.agent(1);
        final TestSupport.Pretplate pretplate = new TestSupport.Pretplate();
        final TestSupport.Zahtevi zahtevi = new TestSupport.Zahtevi();
        final TestSupport.Izdavanja izdavanja = new TestSupport.Izdavanja();
        final PretplataMenadzer menadzer = new PretplataMenadzer(pretplate, zahtevi, izdavanja,
                CenovnikMenadzerTest.menadzerSaCenovnikom());
    }
}
