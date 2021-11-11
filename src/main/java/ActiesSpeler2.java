import java.io.IOException;
import java.util.Scanner;

public class ActiesSpeler2 {

    public static void main(String[] args) throws IOException {
        Speler speler2 = new Speler("naam2", 6665, 6666, false,"jdbc:sqlite:C:/Users/lucas/OneDrive/Documenten/HAN/Jaar 3/ASD-project/Onderzoeken/Data synchronisatie/Server client/Client1/src/main/resources/myDatabase2.db");
        boolean stoppen = false;
        do {
            Scanner input = new Scanner(System.in);
            switch (input.next()) {
                case "e":
                    speler2.databaseExit();
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
                case "l":
                    speler2.receivePlayer();
                    break;
                default:
                    System.out.println("---");
            }
        }
        while (!stoppen);

        speler2.closeSocket();

        System.out.println(speler2.toString());
    }
}

