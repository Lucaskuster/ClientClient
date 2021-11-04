import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Speler1 {
    private String naam;
    private int weekNr;

    // De tijdelijke dataopslag
    private LinkedList<Order> dataopslag1;

    public Speler1(String naam) {
        this.naam = naam;
        this.weekNr = 1;

        dataopslag1 = new LinkedList<>();
    }

    public void plaatsOrder(int aantal) {
        Order order = new Order(weekNr, this.naam, aantal);
        dataopslag1.add(order);
        send(order);
        weekNr++;
    }

    private void send(Order order){
        try {
            Socket s = new Socket("localhost", 6666);

            //DataOutputStream dout=new DataOutputStream(s.getOutputStream());
            //dout.writeUTF("Hello Server");

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(order);

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void receive() {
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
        return "Speler1{" +
                "naam='" + naam + '\'' +
                ", weekNr=" + weekNr +
                ", dataopslag1=" + dataopslag1 +
                '}';
    }
}
