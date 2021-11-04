import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Speler2 {
    private String naam;
    private int week;
    private boolean aanDeBeurt;
    private boolean actief;
    private ServerSocket socket2;

    // De tijdelijke dataopslag
    private final LinkedList<Order> dataopslag2;

    public Speler2(String naam) throws IOException {
        this.naam = naam;
        this.week = 1;
        this.aanDeBeurt = false;
        this.actief = true;
        this.socket2 = new ServerSocket(6666);

        dataopslag2 = new LinkedList<>();
    }

    public void plaatsOrder(int aantal) {
        Order order = new Order(week, this.naam, aantal);
        dataopslag2.add(order);
        send(order);
        week++;
    }

    private void send(Order order) {
        try {
            Socket s = new Socket("localhost", 6665);

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(packaging(order));

            dout.flush();
            dout.close();
            s.close();
            aanDeBeurt = false;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Maakt van de order een te verzenden arraylist
     *
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
            Socket speler2 = socket2.accept();//establishes connection
            ObjectInputStream dis = new ObjectInputStream(speler2.getInputStream());

            List<String> listOfMessages = (List<String>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "] messages from: " + speler2);

            String weekReceived = null;
            String naamReceived = null;
            String aantalReceived = null;

            for (String message : listOfMessages) {
                switch (listOfMessages.indexOf(message)) {
                    case 0:
                        weekReceived = message;
                        break;
                    case 1:
                        naamReceived = message;
                        break;
                    case 2:
                        aantalReceived = message;
                        break;
                    default:
                        System.out.println("Error");
                }
            }
            Order orderReceived = new Order(Integer.parseInt(weekReceived), naamReceived, Integer.parseInt(aantalReceived));
            dataopslag2.add(orderReceived);

            if(naamReceived.equals("naam1"))
                aanDeBeurt = true;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public String toString() {
        return "Speler2{" +
                "naam='" + naam + '\'' +
                ", weekNr=" + week +
                ", dataopslag2=" + dataopslag2 +
                '}';
    }

    public boolean isAanDeBeurt() {
        return aanDeBeurt;
    }

    public boolean isActief() {
        return actief;
    }

    public void setActief(boolean actief) {
        this.actief = actief;
    }

    public void closeSocket() throws IOException {
        socket2.close();
    }
}
