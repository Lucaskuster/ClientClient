import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Speler1 {
    private String naam;
    private int week;

    // De tijdelijke dataopslag
    private LinkedList<Order> dataopslag1;

    public Speler1(String naam) {
        this.naam = naam;
        this.week = 1;

        dataopslag1 = new LinkedList<>();
    }

    public void plaatsOrder(int aantal) {
        Order order = new Order(week, this.naam, aantal);
        dataopslag1.add(order);
        send(order);
        week++;
    }

    private void send(Order order){
        try {
            Socket s = new Socket("localhost", 6666);

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(packaging(order));

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Maakt van de order een te verzenden arraylist
     * @param order
     * @return te verzenden order
     */
    private List<String> packaging(Order order) {
        List<String> output = new ArrayList<>();
        output.add(String.valueOf(order.week));
        output.add(order.naam);
        output.add(String.valueOf((order.order)));
        return output;
    }

    public static void receive() {
        try {
            ServerSocket ss = new ServerSocket(6666);
            Socket s = ss.accept();//establishes connection
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

            List<String> listOfMessages = (List<String>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "] messages from: " + s);

            System.out.println("All messages:");
            for (String message: listOfMessages) {
                switch (listOfMessages.indexOf(message)){
                    case 0:
                        System.out.println("Week: " + message);
                        break;
                    case 1:
                        System.out.println("Naam: " + message);
                        break;
                    case 2:
                        System.out.println("Aantal: " + message);
                        break;
                    default:
                        System.out.println("Einde");
                }
            }

            ss.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public String toString() {
        return "Speler1{" +
                "naam='" + naam + '\'' +
                ", weekNr=" + week +
                ", dataopslag1=" + dataopslag1 +
                '}';
    }
}
