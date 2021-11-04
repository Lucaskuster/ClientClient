public class Order {
    protected int week;
    protected String naam;
    protected int order;

    public Order(int week, String naam, int order) {
        this.week = week;
        this.naam = naam;
        this.order = order;
    }

    @Override
    public String toString() {
        return "Order{" +
                "week=" + week +
                ", naam='" + naam + '\'' +
                ", order=" + order +
                '}';
    }
}
