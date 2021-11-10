public class Order {
    protected int week;
    protected String naam;
    protected int aantal;

    public Order(int week, String naam, int order) {
        this.week = week;
        this.naam = naam;
        this.aantal = order;
    }

    @Override
    public String toString() {
        return "Order{" +
                "week=" + week +
                ", naam='" + naam + '\'' +
                ", order=" + aantal +
                '}';
    }
}
