package menadzment;

import model.Agent;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrijavaMenadzerTest {
    @Test
    public void loginVracaKorisnikaZaTacnePodatke() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();
        Agent agent = TestSupport.agent(1);
        repo.dodaj(agent);

        assertSame(agent, new PrijavaMenadzer(repo).login("agent1", "pass"));
    }

    @Test
    public void loginVracaNullZaPogresnuLozinku() {
        TestSupport.Korisnici repo = new TestSupport.Korisnici();
        repo.dodaj(TestSupport.agent(1));

        assertNull(new PrijavaMenadzer(repo).login("agent1", "pogresna"));
    }

}
