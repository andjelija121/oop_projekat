package repozitorijum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PodesavanjaRepozitorijum {
    private String putanjaDoFajla;

    public PodesavanjaRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/podesavanja.csv";
    }

    public int ucitajPodrazumevanoTrajanjeNajma() {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                String[] delovi = linije.get(i).split(",", -1);
                if (delovi.length >= 2 && "PODRAZUMEVANO_TRAJANJE_NAJMA".equals(delovi[0])) {
                    return Integer.parseInt(delovi[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        } catch (NumberFormatException e) {
            System.out.println("Podrazumevano trajanje najma nije ispravan broj.");
        }

        return 3;
    }

    public void sacuvajPodrazumevanoTrajanjeNajma(int brojDana) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            boolean izmenjeno = false;

            for (int i = 1; i < linije.size(); i++) {
                String[] delovi = linije.get(i).split(",", -1);
                if (delovi.length >= 1 && "PODRAZUMEVANO_TRAJANJE_NAJMA".equals(delovi[0])) {
                    linije.set(i, "PODRAZUMEVANO_TRAJANJE_NAJMA," + brojDana);
                    izmenjeno = true;
                    break;
                }
            }

            if (!izmenjeno) {
                linije.add("PODRAZUMEVANO_TRAJANJE_NAJMA," + brojDana);
            }

            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja fajla: " + putanjaDoFajla);
        }
    }
}
