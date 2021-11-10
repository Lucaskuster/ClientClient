public class PlayerAdress {
    String name;
    int port;

    public PlayerAdress(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "PlayerAdress{" +
                "name='" + name + '\'' +
                ", port=" + port +
                '}';
    }
}
