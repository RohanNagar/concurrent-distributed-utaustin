/*
 * EIDs of group members
 * ran679, kmg2969
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Store implements Serializable {
  private Map<String, Integer> inventory;
  private Map<Integer, Order> orders;
  private Map<String, List<Order>> userOrders;
  private AtomicInteger orderNumber;

  public Store(String inventoryFile) {
    this.inventory = new HashMap<>();
    this.orders = new HashMap<>();
    this.userOrders = new HashMap<>();
    this.orderNumber = new AtomicInteger(1);

    buildInventory(inventoryFile);
  }

  private Store(Map<String, Integer> inventory,
                Map<Integer, Order> orders,
                Map<String, List<Order>> userOrders,
                int orderNumber) {
    this.inventory = inventory;
    this.orders = orders;
    this.userOrders = userOrders;
    this.orderNumber = new AtomicInteger(orderNumber);
  }

  /**
   * Performs a purchase in the online store.
   *
   * @param username The name of the user requesting the purchase
   * @param product  The product being purchased
   * @param quantity The amount of the product being purchased
   * @return The message to send as a reply
   */
  public synchronized String purchase(String username, String product, int quantity) {
    Integer amountInStock = inventory.get(product);

    if (amountInStock == null) {
      return "Not Available - We do not sell this product";
    }

    if (amountInStock < quantity) {
      return "Not Available - Not enough items";
    }

    // Obtain ID
    int id = orderNumber.getAndIncrement();

    Order order = new Order(id, username, product, quantity);

    inventory.replace(product, amountInStock - quantity);
    orders.put(id, order);

    if (userOrders.containsKey(username)) {
      List<Order> orderList = userOrders.get(username);
      orderList.add(order);
    } else {
      List<Order> orderList = new ArrayList<>();
      orderList.add(order);

      userOrders.put(username, orderList);
    }


    return "Your order has been placed, "
        + order.getId() + " "
        + order.getUsername() + " "
        + order.getProduct() + " "
        + order.getQuantity();
  }

  /**
   * Cancels the given order.
   *
   * @param id The ID of the order to cancel
   * @return The message indicating success or failure
   */
  public synchronized String cancel(int id) {
    if (!orders.containsKey(id)) {
      return id + " not found, no such order";
    }

    Order order = orders.get(id);
    Integer amountInStock = inventory.get(order.getProduct());

    inventory.replace(order.getProduct(), amountInStock + order.getQuantity());
    orders.remove(id);

    List<Order> currentOrders = userOrders.get(order.getUsername());
    currentOrders.remove(order);

    return "Order " + id + " is canceled";
  }

  /**
   * Obtains all of the orders for a given user.
   *
   * @param username The user to search for
   * @return A String listing of all of the orders for the requested user
   */
  public synchronized String getOrdersForUser(String username) {
    if (!userOrders.containsKey(username)) {
      return "No order found for " + username;
    }

    List<Order> orders = userOrders.get(username);
    if (orders.size() == 0) {
      return "No order found for " + username;
    }

    StringBuilder builder = new StringBuilder();
    for (Order order : orders) {
      builder.append(order.getId());
      builder.append(", ");
      builder.append(order.getProduct());
      builder.append(", ");
      builder.append(order.getQuantity());
      builder.append("\n");
    }

    // Remove last newline
    builder.deleteCharAt(builder.lastIndexOf("\n"));

    return builder.toString();
  }

  /**
   * Obtains a string representation of the current state of the inventory.
   *
   * @return The inventory as a String
   */
  public synchronized String readInventory() {
    StringBuilder builder = new StringBuilder();

    // Build string with all products
    for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
      builder.append(entry.getKey());
      builder.append(" ");
      builder.append(entry.getValue());
      builder.append("\n");
    }

    // Remove last newline
    builder.deleteCharAt(builder.lastIndexOf("\n"));

    return builder.toString();
  }

  /**
   * Updates the state of this store to be the same as the provided store.
   *
   * @param other The store to update this store's state to.
   */
  public synchronized void updateStore(Store other) {
    this.userOrders = other.userOrders;
    this.orders = other.orders;
    this.inventory = other.inventory;
    this.orderNumber.set(other.orderNumber.get());
  }

  /**
   * Provides a String representation for sending the server over.
   *
   * @return The String representation.
   */
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("INVENTORY ");
    for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
      builder.append(entry.getKey());
      builder.append(":");
      builder.append(entry.getValue());
      builder.append(" ");
    }

    builder.append("ORDERS ");
    for (Map.Entry<Integer, Order> entry : orders.entrySet()) {
      builder.append(entry.getKey());
      builder.append(":");
      builder.append(entry.getValue());
      builder.append(" ");
    }

    builder.append("USER-ORDERS ");
    for (Map.Entry<String, List<Order>> entry : userOrders.entrySet()) {
      builder.append(entry.getKey());
      builder.append(":");

      List<Order> values =  entry.getValue();
      for (Order order : values) {
        builder.append(order);
        builder.append(",");
      }

      builder.append(" ");
    }

    builder.append("NUMBER ");
    builder.append(orderNumber.get());

    return builder.toString();
  }

  /**
   * Creates a new Store instance from a given String representation.
   *
   * @param s The String representation to parse.
   * @return The new Store instance.
   */
  public static Store fromString(String s) {
    String[] tokens = s.split(" ");

    Map<String, Integer> inventory = new HashMap<>();
    Map<Integer, Order> orders = new HashMap<>();
    Map<String, List<Order>> userOrders = new HashMap<>();
    int orderNumber = 1;

    int index = 0;
    while (index < tokens.length) {
      String word = tokens[index];

      if (word.equals("INVENTORY")) {
        int innerIndex = index + 1;
        while (innerIndex < tokens.length) {
          word = tokens[innerIndex];
          if (word.equals("ORDERS")) break;

          String[] innerTokens = word.split(":");

          inventory.put(innerTokens[0], Integer.parseInt(innerTokens[1]));
          innerIndex++;
        }

        index = innerIndex;
      }

      if (word.equals("ORDERS")) {
        int innerIndex = index + 1;
        while (innerIndex < tokens.length) {
          word = tokens[innerIndex];
          if (word.equals("USER-ORDERS")) break;

          String[] innerTokens = word.split(":");

          orders.put(Integer.parseInt(innerTokens[0]), Order.fromString(innerTokens[1]));
          innerIndex++;
        }

        index = innerIndex;
      }

      if (word.equals("USER-ORDERS")) {
        int innerIndex = index + 1;
        while (innerIndex < tokens.length) {
          word = tokens[innerIndex];
          if (word.equals("NUMBER")) break;

          String[] innerTokens = word.split(":");

          List<Order> orderList = new ArrayList<>();

          if (innerTokens.length > 1) {
            String[] orderTokens = innerTokens[1].split(",");
            for (String orderString : orderTokens) {
              orderList.add(Order.fromString(orderString));
            }
          }

          userOrders.put(innerTokens[0], orderList);
          innerIndex++;
        }

        index = innerIndex;
      }

      if (word.equals("NUMBER")) {
        index++;
        orderNumber = Integer.parseInt(tokens[index]);
        index++;
      }
    }

    return new Store(inventory, orders, userOrders, orderNumber);
  }

  /**
   * Initializes an inventory map given a file to read from.
   *
   * @param filename The file with which to populate the inventory
   */
  private void buildInventory(String filename) {
    Scanner sc;
    try {
      sc = new Scanner(new FileReader(filename));
    } catch (FileNotFoundException e) {
      System.out.println("FATAL ERROR: Inventory file not found.");

      System.exit(-1);
      return;
    }

    // Parse the inventory file
    while (sc.hasNextLine()) {
      String product = sc.nextLine();

      String[] tokens = product.split(" ");
      if (tokens.length != 2) {
        continue;
      }

      inventory.put(tokens[0], Integer.parseInt(tokens[1]));
    }
  }
}
