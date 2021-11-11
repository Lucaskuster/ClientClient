import java.io.IOException;
import java.util.Scanner;

public class ActieSpeler3 {

    public static void main(String[] args) throws IOException {
        Speler speler3 = new Speler("naam3", 6664, 6666, false,"jdbc:sqlite:C:/Users/lucas/OneDrive/Documenten/HAN/Jaar 3/ASD-project/Onderzoeken/Data synchronisatie/Server client/Client1/src/main/resources/myDatabase3.db");
        boolean stoppen = false;
        do {
            Scanner input = new Scanner(System.in);
            switch (input.next()) {
                case "e":
                    speler3.databaseExit();
                    stoppen = true;
                    break;
                case "p":
                    if (speler3.isAanDeBeurt())
                        speler3.plaatsOrder(Integer.parseInt(input.next()));
                    break;
                case "o":
                    do {
                        speler3.receiveOrder();
                    } while (!speler3.isAanDeBeurt());
                    break;
                case "l":
                    speler3.receivePlayer();
                    break;
                default:
                    System.out.println("---");
            }
        }
        while (!stoppen);

        speler3.closeSocket();

        System.out.println(speler3.toString());
    }
}
