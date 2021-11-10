import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Speler {
    private final String naam;
    private int week;
    private boolean aanDeBeurt;
    // TODO gebruik maken van actief boolean;
    private boolean actief;
    private final ServerSocket socket;
    private final int portTo;
    private final String jdbcUrl;

    // De tijdelijke dataopslag
    private final LinkedList<Order> dataopslag;

    public Speler(String naam, int portThis, int portTo, String jdbcUrl) throws IOException {
        this.naam = naam;
        this.week = 1;
        this.aanDeBeurt = true;
        this.actief = true;
        this.portTo = portTo;
        this.socket = new ServerSocket(portThis);
        this.jdbcUrl = jdbcUrl;

        dataopslag = new LinkedList<>();
    }

    public void plaatsOrder(int aantal) {

        Order order = new Order(week, this.naam, aantal);
        dataopslag.add(order);
        databaseAddOrder(order);
        send(order);
        week++;
    }

    /**
     * Deze functie verstuurd de geplaatste order naar alle spelers
     * Op dit moment nog naar een andere speler door 'portTo'
     * Ook wordt er op dit moment nog gewerkt met Sockets die ook weg gaan.
     *
     * @param order is de te versturen order. Geplaatst door deze speler.
     */
    private void send(Order order) {
        try {
            Socket s = new Socket("localhost", portTo);

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
     * @param order wordt hier van het Object Order omgeschreven naar een ArrayList
     * @return te verzenden order
     */
    private List<String> packaging(Order order) {
        List<String> output = new ArrayList<>();
        output.add(String.valueOf(order.week));
        output.add(order.naam);
        output.add(String.valueOf((order.aantal)));
        return output;
    }

    /**
     * Ontvangt de verstuurde order van andere spelers en zet deze in de database van deze speler.
     */
    public void receive() {
        try {
            Socket speler = socket.accept(); //establishes connection
            ObjectInputStream dis = new ObjectInputStream(speler.getInputStream());

            List<String> listOfMessages = (List<String>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "] messages from: " + speler);
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
            assert weekReceived != null;
            assert aantalReceived != null;
            Order orderReceived = new Order(Integer.parseInt(weekReceived), naamReceived, Integer.parseInt(aantalReceived));
            dataopslag.add(orderReceived);
            databaseAddOrder(orderReceived);

            aanDeBeurt = true;

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public String toString() {
        return "Speler{" +
                "naam='" + naam + '\'' +
                ", weekNr=" + week +
                ", dataopslag1=" + dataopslag +
                '}';
    }

    public boolean isAanDeBeurt() {
        return aanDeBeurt;
    }


    public void closeSocket() throws IOException {
        socket.close();
    }

    private void databaseAddOrder(Order order) {
        try {
            String week = String.valueOf(order.week);
            String naam = order.naam;
            String aantal = String.valueOf(order.aantal);

            Connection connection = DriverManager.getConnection(jdbcUrl);
            String sql = "INSERT INTO \"order\" VALUES( " + week + ", '" + naam + "', " + aantal + ")";

            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    public void databaseExit() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            String sqlSelect = "SELECT * FROM \"order\"";

            Statement selectStatement = connection.createStatement();
            ResultSet result = selectStatement.executeQuery(sqlSelect);

            while (result.next()) {
                String week = result.getString("week");
                String naam = result.getString("naam");
                String aantal = result.getString("aantal");

                System.out.println(week + " | " + naam + " | " + aantal);
            }

            String sqlDelete = "DELETE FROM \"order\"";

            Statement deleteStatement = connection.createStatement();
            deleteStatement.execute(sqlDelete);
        } catch (SQLException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

}
