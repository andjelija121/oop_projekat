package menadzment;

import enums.KategorijaKlijenta;
import enums.Pol;
import org.junit.Test;

import static org.junit.Assert.*;

public class KlijentMenadzerTest {
    @Test
    public void agentMozeDaDodaKlijenta() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();

        boolean dodat = new KlijentMenadzer(repo).dodajKlijenta(TestSupport.agent(1), "Ime", "Prezime",
                Pol.MUSKI, "1990-01-01", "060", "Adresa", "novi@mail.com", "pass",
                "2020-01-01", KategorijaKlijenta.BEZ_KATEGORIJE);

        assertTrue(dodat);
        assertEquals(1, repo.ucitajSve().size());
    }

    @Test
    public void neAgentNeMozeDaDodaKlijenta() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();

        boolean dodat = new KlijentMenadzer(repo).dodajKlijenta(TestSupport.admin(1), "Ime", "Prezime",
                Pol.MUSKI, "1990-01-01", "060", "Adresa", "novi@mail.com", "pass",
                "2020-01-01", KategorijaKlijenta.BEZ_KATEGORIJE);

        assertFalse(dodat);
        assertEquals(0, repo.ucitajSve().size());
    }

    @Test
    public void dupliEmailSeNeDodaje() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();
        repo.dodaj(TestSupport.klijent(1));

        boolean dodat = new KlijentMenadzer(repo).dodajKlijenta(TestSupport.agent(1), "Ime", "Prezime",
                Pol.MUSKI, "1990-01-01", "060", "Adresa", "klijent1", "pass",
                "2020-01-01", KategorijaKlijenta.BEZ_KATEGORIJE);

        assertFalse(dodat);
    }
}
