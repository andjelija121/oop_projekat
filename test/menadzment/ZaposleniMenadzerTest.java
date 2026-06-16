package menadzment;

import enums.NivoSpreme;
import enums.Pol;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZaposleniMenadzerTest {
    @Test
    public void administratorMozeDaDodaAgenta() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();

        boolean dodat = new ZaposleniMenadzer(repo).dodajAgenta(TestSupport.admin(1), "Agent", "Novi",
                Pol.MUSKI, "1990-01-01", "060", "Adresa", "noviAgent", "pass",
                NivoSpreme.VI, 3, 90000);

        assertTrue(dodat);
        assertEquals(1, repo.ucitajAgente().size());
    }

    @Test
    public void administratorMozeDaDodaAdministratora() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();

        boolean dodat = new ZaposleniMenadzer(repo).dodajAdministratora(TestSupport.admin(1), "Admin",
                "Novi", Pol.ZENSKI, "1980-01-01", "061", "Adresa", "noviAdmin", "pass",
                NivoSpreme.VII, 8, 120000);

        assertTrue(dodat);
        assertEquals(1, repo.ucitajAdministratore().size());
    }

    @Test
    public void neAdministratorNeMozeDaDodajeZaposlene() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();

        boolean dodat = new ZaposleniMenadzer(repo).dodajAgenta(TestSupport.agent(1), "Agent", "Novi",
                Pol.MUSKI, "1990-01-01", "060", "Adresa", "noviAgent", "pass",
                NivoSpreme.VI, 3, 90000);

        assertFalse(dodat);
    }

    @Test
    public void duploKorisnickoImeZaposlenogSeOdbija() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();
        repo.dodaj(TestSupport.agent(1));

        boolean dodat = new ZaposleniMenadzer(repo).dodajAgenta(TestSupport.admin(1), "Agent", "Novi",
                Pol.MUSKI, "1990-01-01", "060", "Adresa", "agent1", "pass",
                NivoSpreme.VI, 3, 90000);

        assertFalse(dodat);
    }
}
