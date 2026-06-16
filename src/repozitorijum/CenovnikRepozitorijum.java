package repozitorijum;

import enums.KategorijaKlijenta;
import enums.KategorijaVozila;
import enums.TipCene;
import model.Cenovnik;
import model.DodatnaUsluga;
import model.StavkaCenovnika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

public class CenovnikRepozitorijum {
    private String putanjaDoFajla;
    private DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;

    public CenovnikRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/cenovnik.csv";
        this.dodatnaUslugaRepozitorijum = new DodatnaUslugaRepozitorijum();
    }

    public CenovnikRepozitorijum(String putanjaDoFajla, DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
    }

    public ArrayList<Cenovnik> ucitajSve() {
        ArrayList<Cenovnik> cenovnici = new ArrayList<>();
        try{
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i=1;i<linije.size();i++){

                String[] delovi=linije.get(i).trim().split(",",-1);
                LocalDate datumOd = LocalDate.parse(delovi[6]);
                LocalDate datumDo = LocalDate.parse(delovi[7]);

                Cenovnik noviCenovnik = null;
                for (Cenovnik c:cenovnici){
                    if (c.getDatumOd().equals(datumOd) && c.getDatumDo().equals(datumDo)) {
                        noviCenovnik = c;
                        break;
                    }
                }
                if(noviCenovnik==null){
                    noviCenovnik = new Cenovnik(Integer.parseInt(delovi[0]),datumOd,datumDo);
                    cenovnici.add(noviCenovnik);
                }



                KategorijaVozila kategorijaVozila = null;
                if (!delovi[3].isBlank()) {
                    kategorijaVozila = KategorijaVozila.valueOf(delovi[3]);
                }

                KategorijaKlijenta kategorijaKlijenta = null;
                if (!delovi[4].isBlank()) {
                    kategorijaKlijenta = KategorijaKlijenta.valueOf(delovi[4]);
                }

                DodatnaUsluga dodatnaUsluga = null;
                if (!delovi[5].isBlank()) {
                    dodatnaUsluga = dodatnaUslugaRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[5]));
                }

                StavkaCenovnika stavka = new StavkaCenovnika(TipCene.valueOf(delovi[1]),Double.parseDouble(delovi[2]),
                        kategorijaVozila,kategorijaKlijenta,dodatnaUsluga);

                noviCenovnik.dodajStavku(stavka);
            }
        }catch(IOException e){
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }
        return cenovnici;
    }

    public Cenovnik pronadjiVazeciCenovnik(LocalDate datum) {
        for (Cenovnik cenovnik : ucitajSve()) {
            if (cenovnik.vaziNaDatum(datum)) {
                return cenovnik;
            }
        }

        return null;
    }

    public Cenovnik pronadjiPoId(int id) {
        for (Cenovnik cenovnik : ucitajSve()) {
            if (cenovnik.getId() == id) {
                return cenovnik;
            }
        }

        return null;
    }

    public void dodaj(Cenovnik cenovnik) {
        ArrayList<Cenovnik> cenovnici = ucitajSve();
        cenovnici.add(cenovnik);
        sacuvajSve(cenovnici);
    }

    public void azuriraj(Cenovnik cenovnik) {
        ArrayList<Cenovnik> cenovnici = ucitajSve();
        for (int i = 0; i < cenovnici.size(); i++) {
            if (cenovnici.get(i).getId() == cenovnik.getId()) {
                cenovnici.set(i, cenovnik);
                break;
            }
        }
        sacuvajSve(cenovnici);
    }

    public void obrisi(int id) {
        ArrayList<Cenovnik> cenovnici = ucitajSve();
        for (int i = cenovnici.size() - 1; i >= 0; i--) {
            if (cenovnici.get(i).getId() == id) {
                cenovnici.remove(i);
                break;
            }
        }
        sacuvajSve(cenovnici);
    }

    public void sacuvajSve(ArrayList<Cenovnik> cenovnici) {
        try {
            ArrayList<Cenovnik> sortirani = new ArrayList<>(cenovnici);
            sortirani.sort(Comparator.comparing(Cenovnik::getDatumOd));

            ArrayList<String> linije = new ArrayList<>();
            linije.add("id,tip,cena,kategorijaVozila,kategorijaKlijenta,dodatnaUslugaId,datumOd,datumDo");

            for (Cenovnik cenovnik : sortirani) {
                for (StavkaCenovnika stavka : cenovnik.getStavke()) {
                    linije.add(napraviCsvLiniju(stavka, cenovnik));
                }
            }

            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja fajla: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;

        for (Cenovnik cenovnik : ucitajSve()) {
            if (cenovnik.getId() > najveciId) {
                najveciId = cenovnik.getId();
            }
        }

        return najveciId + 1;
    }

    private String napraviCsvLiniju(StavkaCenovnika stavka, Cenovnik cenovnik) {
        String kategorijaVozila = stavka.getKategorijaVozila() == null ? "" : stavka.getKategorijaVozila().name();
        String kategorijaKlijenta = stavka.getKategorijaKlijenta() == null ? ""
                : stavka.getKategorijaKlijenta().name();
        String dodatnaUslugaId = stavka.getDodatnaUsluga() == null ? ""
                : String.valueOf(stavka.getDodatnaUsluga().getId());

        return cenovnik.getId() + "," + stavka.getTipCene() + "," + stavka.getVrednost() + ","
                + kategorijaVozila + "," + kategorijaKlijenta + "," + dodatnaUslugaId + ","
                + cenovnik.getDatumOd() + "," + cenovnik.getDatumDo();
    }
}
