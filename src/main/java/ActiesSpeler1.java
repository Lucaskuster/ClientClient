import java.io.IOException;
import java.util.Scanner;

public class ActiesSpeler1 {

    public static void main(String[] args) throws IOException {

        Speler speler1 = new Speler("naam1", 6666, 6665, "jdbc:sqlite:C:/Users/lucas/OneDrive/Documenten/HAN/Jaar 3/ASD-project/SQLite/myDatabase1.db");
        boolean stoppen = false;

        do {
            Scanner input = new Scanner(System.in);
            switch (input.next()) {
                case "e":
                    speler1.databaseExit();
                    stoppen = true;
                    break;
                case "p":
                    if (speler1.isAanDeBeurt())
                        speler1.plaatsOrder(Integer.parseInt(input.next()));
                    break;
                case "o":
                    do {
                        speler1.receive();
                    } while (!speler1.isAanDeBeurt());
                    break;
                default:
                    System.out.println("---");
            }
        }
        while (!stoppen);

        speler1.closeSocket();

        System.out.println(speler1.toString());
    }
}


