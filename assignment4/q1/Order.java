/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.io.Serializable;

public class Order implements Serializable {
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

  public String toString() {
    return id + "!" + username + "!" + product + "!" + quantity;
  }

  public static Order fromString(String s) {
    String[] tokens = s.split("!");

    Integer id = Integer.parseInt(tokens[0]);
    String username = tokens[1];
    String product = tokens[2];
    Integer quantity = Integer.parseInt(tokens[3]);

    return new Order(id, username, product, quantity);
  }
}