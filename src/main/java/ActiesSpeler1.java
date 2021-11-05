import java.io.IOException;
import java.util.Scanner;

public class ActiesSpeler1 {

    public static void main(String[] args) throws IOException {
        Speler speler1 = new Speler("naam1", 6666);
        boolean stoppen = false;

        do {
            Scanner input = new Scanner(System.in);
            switch (input.next()) {
                case "e":
                    stoppen = true;
                    break;
                case "p":
                    if (speler1.isAanDeBeurt())
                        speler1.plaatsOrder(Integer.parseInt(input.next()));
                    break;
                case "o":
                    do {
                        speler1.receiveOrder();
                    } while (!speler1.isAanDeBeurt());
                    break;
                case "n":
                    speler1.newPlayers();
                    break;
                case "l":
                    speler1.re
                default:
                    System.out.println("---");
            }
        }
        while (!stoppen);

        System.out.println(speler1.toString());
    }
}
