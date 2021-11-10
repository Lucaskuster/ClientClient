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
    private final ServerSocket socket;
    private final int portTo;
    private final int portThis;
    private final String jdbcUrl;

    // De tijdelijke dataopslag
    private final LinkedList<Order> dataopslag;

    public Speler(String naam, int portThis, int portTo, boolean aanDeBeurt, String jdbcUrl) throws IOException {
        this.naam = naam;
        this.week = 1;
        this.aanDeBeurt = aanDeBeurt;
        this.portTo = portTo;
        this.portThis = portThis;
        this.socket = new ServerSocket(portThis);
        this.jdbcUrl = jdbcUrl;

        dataopslag = new LinkedList<>();

        if (portThis != 6666) {
            sendPlayer();
        } else {
            databaseAddPlayer(naam, portThis);
        }
    }

    /**
     * Met deze methode plaatst de speler een bestelling en roept send() aan om de bestelling naar alle andere spelers te sturen.
     *
     * @param aantal is het aantal units dat de speler wil bestellen.
     */
    public void plaatsOrder(int aantal) {
        Order order = new Order(week, this.naam, aantal);
        dataopslag.add(order);
        databaseAddOrder(order);
        sendOrder(order);
        week++;
    }

    /**
     * Deze functie verstuurd de geplaatste order naar alle spelers
     * Op dit moment nog naar een andere speler door 'portTo'
     * Ook wordt er op dit moment nog gewerkt met Sockets die ook weg gaan.
     *
     * @param order is de te versturen order. Geplaatst door deze speler.
     */
    private void sendOrder(Order order) {
        try {
            Socket s = new Socket("localhost", portTo);

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(packaging(order));

            dout.flush();
            dout.close();
            s.close();
            aanDeBeurt = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verstuurt eigen adress gegevens naar startplayer.
     */
    private void sendPlayer() {
        try {
            Socket s = new Socket("localhost", portTo);

            List<String> player = new ArrayList<>();
            player.add(naam);
            player.add(String.valueOf(portThis));

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(player);

            dout.flush();
            dout.close();
            s.close();
            receivePlayers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verstuurt alle adressgegevens van alle spelers naar de nieuwe speler
     */
    private void sendPlayers(int port) {
        try {
            Socket s = new Socket("localhost", port);

            List<PlayerAdress> players = databaseGetPlayers();
            List<String> player = new ArrayList<>();

            assert players != null;
            for (PlayerAdress playerAdress : players) {
                player.add(playerAdress.getName());
                player.add(String.valueOf(playerAdress.getPort()));
            }

            ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
            dout.writeObject(player);

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
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
    public void receiveOrder() {
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

            beurtWissel(orderReceived.naam);

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void receivePlayer() {
        try {
            Socket speler = socket.accept(); //establishes connection
            ObjectInputStream dis = new ObjectInputStream(speler.getInputStream());

            List<String> listOfMessages = (List<String>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "] messages from: " + speler);

            String naamReceived = null;
            String portReceived = null;

            for (String message : listOfMessages) {
                switch (listOfMessages.indexOf(message)) {
                    case 0:
                        naamReceived = message;
                        break;
                    case 1:
                        portReceived = message;
                        databaseAddPlayer(naamReceived, Integer.parseInt(portReceived));
                        break;
                    default:
                        System.out.println("Error");
                }
            }

            assert portReceived != null;
            sendPlayers(Integer.parseInt(portReceived));

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    private void receivePlayers() {
        try {
            Socket speler = socket.accept(); //establishes connection
            ObjectInputStream dis = new ObjectInputStream(speler.getInputStream());

            List<String> listOfMessages = (List<String>) dis.readObject();
            System.out.println("Received [" + listOfMessages.size() + "] messages from: " + speler);

            int i = 0;
            while (i < listOfMessages.size()) {
                databaseAddPlayer(listOfMessages.get(i), Integer.parseInt(listOfMessages.get(i + 1)));
                //sendPlayers(Integer.parseInt(listOfMessages.get(i + 1)));
                i += 2;
            }

        } catch (
                Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    /**
     * Kijkt of de vorige speler de speler voor deze speler is.
     * Zo ja, dan is deze speler aan de beurt.
     *
     * @param vorigeSpeler de speler die de laatste zet heeft gezet.
     */
    private void beurtWissel(String vorigeSpeler) {
        //TODO afvragen hoe duur het is om bij elke beurt wissel iedereen de lijst met namen op te halen uit de database.
        List<PlayerAdress> spelers = databaseGetPlayers();
        List<String> namen = new ArrayList<>();

        assert spelers != null;
        for (PlayerAdress speler : spelers) {
            namen.add(speler.getName());
        }

        for (String naam : namen) {
            if (namen.get(naam.indexOf(naam) - 1).equals(vorigeSpeler)) {
                aanDeBeurt = true;
            }
        }
    }

    /*
    for (String naam : spelers) {
            if (spelers.get(naam.indexOf(naam) - 1).equals(vorigeSpeler)) {
                aanDeBeurt = true;
            }
        }
     */

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

    private void databaseAddPlayer(String naam, int port) {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            String sql = "INSERT INTO \"spelers\" VALUES( '" + naam + "', " + port + ")";

            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }

    private List<PlayerAdress> databaseGetPlayers() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            String sqlSelect = "SELECT * FROM main.spelers";

            Statement selectStatement = connection.createStatement();
            ResultSet result = selectStatement.executeQuery(sqlSelect);

            List<PlayerAdress> players = new ArrayList<>();

            while (result.next()) {
                String naam = result.getString("naam");
                int port = result.getInt("port");
                PlayerAdress speler = new PlayerAdress(naam, port);
                players.add(speler);
            }

            return players;
        } catch (SQLException e) {
            System.out.println("error");
            e.printStackTrace();
        }
        //TODO wat anders voor neerzetten
        return null;
    }

    public void databaseExit() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            String sqlSelectOrders = "SELECT * FROM \"order\"";

            Statement selectOrderStatement = connection.createStatement();
            ResultSet resultOrder = selectOrderStatement.executeQuery(sqlSelectOrders);

            while (resultOrder.next()) {
                String week = resultOrder.getString("week");
                String naam = resultOrder.getString("naam");
                String aantal = resultOrder.getString("aantal");

                System.out.println(week + " | " + naam + " | " + aantal);
            }

            String sqlOrderDelete = "DELETE FROM \"order\"";

            Statement deleteOrderStatement = connection.createStatement();
            deleteOrderStatement.execute(sqlOrderDelete);


            String sqlSelectPlayers = "SELECT * FROM main.spelers";

            Statement selectPlayerStatement = connection.createStatement();
            ResultSet resultPlayer = selectOrderStatement.executeQuery(sqlSelectPlayers);

            while (resultPlayer.next()) {
                String name = resultPlayer.getString("naam");
                String port = resultPlayer.getString("port");

                System.out.println(name + " | " + port);
            }

            String sqlPlayerDelete = "DELETE FROM spelers";

            Statement deletePlayerStatement = connection.createStatement();
            deletePlayerStatement.execute(sqlPlayerDelete);
        } catch (SQLException e) {
            System.out.println("error");
            e.printStackTrace();
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
}
