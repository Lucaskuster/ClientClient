import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Speler {
    private final String naam;
    private int week;
    private boolean aanDeBeurt;
    private boolean actief;
    //private final ServerSocket socket;
    private final int port;

    // De tijdelijke dataopslag
    private final LinkedList<Order> dataopslag;
    private LinkedList<Integer> ports;

    public Speler(String naam, int port) throws IOException {
        this.naam = naam;
        this.week = 1;
        this.aanDeBeurt = true;
        this.actief = true;
        //this.socket = new ServerSocket(port);
        this.port = port;

        dataopslag = new LinkedList<>();
        ports = new LinkedList<>();
        ports.add(port);
        joinGame();
    }

    private void joinGame() {
        if(!(this.port == 6666)) {

            // Het adres van de starter of het bekende adres in de game.
            send(this.ports, 6666);

            // Wachten tot je geaccepteerd bent, dan krijg je alle poorten
            List<Integer> ports = receive();

            assert ports != null;
            this.ports = (LinkedList<Integer>) ports;

            // Stuur alle poorten inclusief die van de speler zelf naar alle andere spelers
            //sendPorts();
            // Probleem er moeten dan wel personen zijn die luisteren.
        }
    }

    /**
     * Stuurt de nieuwe speler alle spelerspoorten
     */
    public void newPlayers() {
        List<Integer> playerPort = receive();
        this.ports.addAll(playerPort);
        assert playerPort != null;
        send(ports, playerPort.get(0));
    }

    private void sendPorts() {
        for (int port : ports) {
            if (port == this.port)
                break;

            send(ports, port);
        }
    }

    private void sendOrder(Order order) {
        for (int port : ports) {
            if (port == this.port)
                break;

            send(packaging(order), port);
            aanDeBeurt = false;
        }
    }

    /**
     * Stuurt gegevens naar een bepaalt adres.
     *
     * @param toSend het adres waar de gegevens heen gaan.
     * @param sendTo wat er gestuurd gaat worden, type maakt niet uit.
     * @throws IOException
     */
    private <AnyType> void send(AnyType toSend, int sendTo) {
        try {
            Socket s = new Socket("localhost", sendTo);

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(toSend);

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void plaatsOrder(int aantal) {
        Order order = new Order(week, this.naam, aantal);
        dataopslag.add(order);
        sendOrder(order);
        week++;
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

    private <AnyType> List<AnyType> receive() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            Socket speler = serverSocket.accept(); //establishes connection
            ObjectInputStream dis = new ObjectInputStream(speler.getInputStream());

            List<AnyType> listOfMessages = (List<AnyType>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "]");

            serverSocket.close();
            return listOfMessages;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public void receiveOrder() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            Socket speler = serverSocket.accept(); //establishes connection
            ObjectInputStream dis = new ObjectInputStream(speler.getInputStream());

            List<String> listOfMessages = (List<String>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "] messages from: " + naam);
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
            dataopslag.add(orderReceived);
            serverSocket.close();

            //TODO aandebeurt
            aanDeBeurt = true;

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public String toString() {
        return "Speler{" +
                "naam='" + naam + '\'' +
                ", week=" + week +
                ", aanDeBeurt=" + aanDeBeurt +
                ", actief=" + actief +
                ", port=" + port +
                ", dataopslag=" + dataopslag +
                ", ports=" + ports +
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
}
