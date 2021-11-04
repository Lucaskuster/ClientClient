import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

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
            dout.writeObject(order);
            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void receive() {
        try {
            ServerSocket ss = new ServerSocket(6666);
            Socket s = ss.accept();//establishes connection
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
            Order orderFromPlayer = (Order) dis.readObject();

            Order order = new Order(orderFromPlayer.week, orderFromPlayer.naam, orderFromPlayer.order);
            System.out.println("message= " + order);
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
