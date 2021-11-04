import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Speler2 {
    private String naam;
    private int weekNr;

    // De tijdelijke dataopslag
    private LinkedList<Order> dataopslag2;

    public Speler2(String naam) {
        this.naam = naam;
        this.weekNr = 1;

        dataopslag2 = new LinkedList<Order>();
    }

    public void plaatsOrder(int aantal) {
        Order order = new Order(weekNr, this.naam, aantal);
        dataopslag2.add(order);
        send(order);
        weekNr++;
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

    public void receive() {
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
        return "Speler2{" +
                "naam='" + naam + '\'' +
                ", weekNr=" + weekNr +
                ", dataopslag2=" + dataopslag2 +
                '}';
    }
}
