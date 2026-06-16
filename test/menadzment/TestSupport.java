package menadzment;

import enums.*;
import model.*;
import repozitorijum.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;

final class TestSupport {
    private TestSupport() {
    }

    static Agent agent(int id) {
        return new Agent(id, "Agent", "Test", Pol.MUSKI, "1990-01-01", "060", "Adresa",
                "agent" + id, "pass", NivoSpreme.VI, 5, 100000);
    }

    static Administrator admin(int id) {
        return new Administrator(id, "Admin", "Test", Pol.ZENSKI, "1985-01-01", "061", "Adresa",
                "admin" + id, "pass", NivoSpreme.VII, 10, 120000);
    }

    static Klijent klijent(int id) {
        return new Klijent(id, "Klijent", "Test", Pol.MUSKI, "1995-01-01", "062", "Adresa",
                "klijent" + id, "pass", LocalDate.now().minusYears(3).toString(),
                KategorijaKlijenta.BEZ_KATEGORIJE);
    }

    static Klijent student(int id) {
        Klijent klijent = klijent(id);
        klijent.setPosebnaKategorija(KategorijaKlijenta.STUDENT);
        return klijent;
    }

    static ModelVozila model(int id, KategorijaVozila kategorija) {
        return new ModelVozila(id, "Golf " + id, "VW", kategorija);
    }

    static DodatnaUsluga gps() {
        return new DodatnaUsluga(1, "GPS", TipNaplate.JEDNOKRATNO);
    }

    static DodatnaUsluga produzenoKoriscenje() {
        return new DodatnaUsluga(2, "PRODUZENO_KORISCENJE", TipNaplate.PO_DANU);
    }

    static Cenovnik cenovnik(LocalDate datumOd, LocalDate datumDo) {
        Cenovnik cenovnik = new Cenovnik(1, datumOd, datumDo);
        for (KategorijaVozila kategorija : KategorijaVozila.values()) {
            cenovnik.dodajStavku(new StavkaCenovnika(TipCene.NAJAM_PO_DANU, 1000, kategorija, null, null));
        }
        for (KategorijaKlijenta kategorija : KategorijaKlijenta.values()) {
            double popust = kategorija == KategorijaKlijenta.STUDENT ? 20 : 0;
            cenovnik.dodajStavku(new StavkaCenovnika(TipCene.POPUST_KLIJENTA, popust, null, kategorija, null));
        }
        cenovnik.dodajStavku(new StavkaCenovnika(TipCene.DODATNA_USLUGA, 300, null, null, gps()));
        cenovnik.dodajStavku(new StavkaCenovnika(TipCene.DODATNA_USLUGA, 500, null, null, produzenoKoriscenje()));
        cenovnik.dodajStavku(new StavkaCenovnika(TipCene.KAZNA_KASNJENJA, 700, null, null, null));
        cenovnik.dodajStavku(new StavkaCenovnika(TipCene.GODISNJA_PRETPLATA, 12000, null, null, null));
        return cenovnik;
    }

    static Rezervacija rezervacija(int id, Klijent klijent, ModelVozila model, StatusRezervacije status,
                                   LocalDate datumOd, LocalDate datumDo) {
        return new Rezervacija(id, klijent, model, datumOd, datumDo, status, 3000, 200, 0, 3200);
    }

    static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static class Korisnici extends KorisnikRepozitorijum {
        final ArrayList<Korisnik> korisnici = new ArrayList<>();

        @Override
        public ArrayList<Korisnik> ucitajSve() {
            return new ArrayList<>(korisnici);
        }

        @Override
        public ArrayList<Agent> ucitajAgente() {
            ArrayList<Agent> rezultat = new ArrayList<>();
            for (Korisnik korisnik : korisnici) {
                if (korisnik instanceof Agent) {
                    rezultat.add((Agent) korisnik);
                }
            }
            return rezultat;
        }

        @Override
        public ArrayList<Administrator> ucitajAdministratore() {
            ArrayList<Administrator> rezultat = new ArrayList<>();
            for (Korisnik korisnik : korisnici) {
                if (korisnik instanceof Administrator) {
                    rezultat.add((Administrator) korisnik);
                }
            }
            return rezultat;
        }

        @Override
        public boolean korisnickoImePostoji(String korisnickoIme) {
            for (Korisnik korisnik : korisnici) {
                if (korisnik.getKorisnickoIme().equals(korisnickoIme)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void dodaj(Korisnik korisnik) {
            if (korisnik.getId() == 0) {
                korisnik.setId(korisnici.size() + 1);
            }
            korisnici.add(korisnik);
        }
    }

    static class Modeli extends ModelVozilaRepozitorijum {
        final ArrayList<ModelVozila> modeli = new ArrayList<>();

        @Override
        public ArrayList<ModelVozila> ucitajSve() {
            return new ArrayList<>(modeli);
        }

        @Override
        public ModelVozila pronadjiPoId(int id) {
            for (ModelVozila model : modeli) {
                if (model.getId() == id) {
                    return model;
                }
            }
            return null;
        }
    }

    static class Vozila extends VoziloRepozitorijum {
        final ArrayList<Vozilo> vozila = new ArrayList<>();

        @Override
        public ArrayList<Vozilo> ucitajSve() {
            return new ArrayList<>(vozila);
        }

        @Override
        public ArrayList<Vozilo> pronadjiPoModelu(int modelId) {
            ArrayList<Vozilo> rezultat = new ArrayList<>();
            for (Vozilo vozilo : vozila) {
                if (vozilo.getModelVozila() != null && vozilo.getModelVozila().getId() == modelId) {
                    rezultat.add(vozilo);
                }
            }
            return rezultat;
        }

        @Override
        public Vozilo pronadjiPoId(int id) {
            for (Vozilo vozilo : vozila) {
                if (vozilo.getId() == id) {
                    return vozilo;
                }
            }
            return null;
        }

        @Override
        public void azuriraj(Vozilo vozilo) {
        }
    }

    static class Rezervacije extends RezervacijaRepozitorijum {
        final ArrayList<Rezervacija> rezervacije = new ArrayList<>();

        @Override
        public ArrayList<Rezervacija> ucitajSve() {
            return new ArrayList<>(rezervacije);
        }

        @Override
        public Rezervacija pronadjiPoId(int id) {
            for (Rezervacija rezervacija : rezervacije) {
                if (rezervacija.getId() == id) {
                    return rezervacija;
                }
            }
            return null;
        }

        @Override
        public void dodaj(Rezervacija rezervacija) {
            rezervacije.add(rezervacija);
        }

        @Override
        public void azuriraj(Rezervacija rezervacija) {
        }

        @Override
        public int sledeciId() {
            return rezervacije.size() + 1;
        }
    }

    static class RezervacijaUsluge extends RezervacijaUslugaRepozitorijum {
        final ArrayList<RezervacijaUsluga> usluge = new ArrayList<>();

        @Override
        public ArrayList<RezervacijaUsluga> ucitajSve() {
            return new ArrayList<>(usluge);
        }

        @Override
        public ArrayList<RezervacijaUsluga> pronadjiPoRezervaciji(int rezervacijaId) {
            ArrayList<RezervacijaUsluga> rezultat = new ArrayList<>();
            for (RezervacijaUsluga usluga : usluge) {
                if (usluga.getRezervacijaId() == rezervacijaId) {
                    rezultat.add(usluga);
                }
            }
            return rezultat;
        }

        @Override
        public void dodaj(RezervacijaUsluga rezervacijaUsluga) {
            usluge.add(rezervacijaUsluga);
        }

        @Override
        public int sledeciId() {
            return usluge.size() + 1;
        }
    }

    static class Izdavanja extends IzdavanjeRepozitorijum {
        final ArrayList<Izdavanje> izdavanja = new ArrayList<>();

        @Override
        public ArrayList<Izdavanje> ucitajSve() {
            return new ArrayList<>(izdavanja);
        }

        @Override
        public Izdavanje pronadjiPoId(int id) {
            for (Izdavanje izdavanje : izdavanja) {
                if (izdavanje.getId() == id) {
                    return izdavanje;
                }
            }
            return null;
        }

        @Override
        public Izdavanje pronadjiPoRezervaciji(int rezervacijaId) {
            for (Izdavanje izdavanje : izdavanja) {
                if (izdavanje.getRezervacija() != null && izdavanje.getRezervacija().getId() == rezervacijaId) {
                    return izdavanje;
                }
            }
            return null;
        }

        @Override
        public void dodaj(Izdavanje izdavanje) {
            izdavanja.add(izdavanje);
        }

        @Override
        public void azuriraj(Izdavanje izdavanje) {
        }

        @Override
        public int sledeciId() {
            return izdavanja.size() + 1;
        }
    }

    static class Cenovnici extends CenovnikRepozitorijum {
        ArrayList<Cenovnik> cenovnici = new ArrayList<>();

        @Override
        public ArrayList<Cenovnik> ucitajSve() {
            return new ArrayList<>(cenovnici);
        }

        @Override
        public Cenovnik pronadjiVazeciCenovnik(LocalDate datum) {
            for (Cenovnik cenovnik : cenovnici) {
                if (cenovnik.vaziNaDatum(datum)) {
                    return cenovnik;
                }
            }
            return null;
        }

        @Override
        public void sacuvajSve(ArrayList<Cenovnik> cenovnici) {
            this.cenovnici = new ArrayList<>(cenovnici);
        }

        @Override
        public int sledeciId() {
            return cenovnici.size() + 1;
        }
    }

    static class Podesavanja extends PodesavanjaRepozitorijum {
        int trajanje = 3;

        @Override
        public int ucitajPodrazumevanoTrajanjeNajma() {
            return trajanje;
        }

        @Override
        public void sacuvajPodrazumevanoTrajanjeNajma(int brojDana) {
            trajanje = brojDana;
        }
    }

    static class Pretplate extends PretplataRepozitorijum {
        final ArrayList<Pretplata> pretplate = new ArrayList<>();

        @Override
        public ArrayList<Pretplata> ucitajSve() {
            return new ArrayList<>(pretplate);
        }

        @Override
        public ArrayList<Pretplata> pronadjiPoKlijentu(int klijentId) {
            ArrayList<Pretplata> rezultat = new ArrayList<>();
            for (Pretplata pretplata : pretplate) {
                if (pretplata.getKlijent() != null && pretplata.getKlijent().getId() == klijentId) {
                    rezultat.add(pretplata);
                }
            }
            return rezultat;
        }

        @Override
        public void dodaj(Pretplata pretplata) {
            pretplate.add(pretplata);
        }

        @Override
        public int sledeciId() {
            return pretplate.size() + 1;
        }
    }

    static class Zahtevi extends ZahtevPretplateRepozitorijum {
        final ArrayList<ZahtevPretplate> zahtevi = new ArrayList<>();

        @Override
        public ArrayList<ZahtevPretplate> ucitajSve() {
            return new ArrayList<>(zahtevi);
        }

        @Override
        public ZahtevPretplate pronadjiPoId(int id) {
            for (ZahtevPretplate zahtev : zahtevi) {
                if (zahtev.getId() == id) {
                    return zahtev;
                }
            }
            return null;
        }

        @Override
        public void dodaj(ZahtevPretplate zahtev) {
            zahtevi.add(zahtev);
        }

        @Override
        public void azuriraj(ZahtevPretplate zahtev) {
        }

        @Override
        public int sledeciId() {
            return zahtevi.size() + 1;
        }
    }
}
