public class Order {
  private final int id;
  private final String username;
  private final String product;
  private final int quantity;

  public Order(int id, String username, String product, int quantity) {
    this.id = id;
    this.username = username;
    this.product = product;
    this.quantity = quantity;
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getProduct() {
    return product;
  }

  public int getQuantity() {
    return quantity;
  }
}
