import java.io.IOException;
import java.util.Scanner;

public class ActiesSpeler2 {

    public static void main(String[] args) throws IOException {
        Speler speler2 = new Speler("naam2", 6665);

        boolean stoppen = false;
        do {
            Scanner input = new Scanner(System.in);
            switch (input.next()) {
                case "e":
                    stoppen = true;
                    break;
                case "p":
                    if (speler2.isAanDeBeurt())
                        speler2.plaatsOrder(Integer.parseInt(input.next()));
                    break;
                case "o":
                    do {
                        speler2.receiveOrder();
                    } while (!speler2.isAanDeBeurt());
                    break;
                default:
                    System.out.println("---");
            }
        }
        while (!stoppen);

        System.out.println(speler2.toString());
    }
}

