package zork;

import java.util.ArrayList;

public class Inventory {
  public static final String yellow = "\u001B[33m";  //for displayInventory
  public static final String white = "\u001B[37m";  //for displayInventory
  public static final String blue = "\u001B[34m";   //for another function



  private ArrayList<Item> items;
  private int maxWeight;
  private int currentWeight;

  public Inventory(int maxWeight) {
    this.items = new ArrayList<Item>();
    this.maxWeight = maxWeight;
    this.currentWeight = 0;
  }

  public int getMaxWeight() {
    return maxWeight;
  }

  public int getCurrentWeight() {
    return currentWeight;
  }

  public boolean add(Item item) {

    if (item.getWeight() + currentWeight <= maxWeight) {
      return items.add(item);

    } else {
      System.out.println();
      System.out.println("There is no room to add this item");
      System.out.println();
    }
    return false;
  }

  //not needed but keeping for now 
  public boolean hasItem(){

    if(items.size() == 0){
      System.out.println();
      System.out.println("This room has no items");
      System.out.println();
    }
    for(int i = 0; i < items.size(); i++){
      System.out.println(i + " ");
    }
    return true;
  }

  public void displayInventory() throws InterruptedException{

    String message =  yellow +"Displaying Inventory" + white ;
    System.out.println();
    System.out.println();

    for(int i = 0; i < message.length(); i++){

      System.out.printf("%c", message.charAt(i) );
      Thread.sleep(50);
    }
    System.out.println();
    System.out.println();

    for(Item i : items){
        System.out.println(i.getName() + " ");
    }
  }

  public void searchRoom() throws InterruptedException{
    String noItems =  blue + "This room has no items" + white;
    String youFound = blue +  "You Found: " + white;

    if(items.size() == 0){
      System.out.println();
        for(int i = 0; i < noItems.length(); i++){
          System.out.printf("%c", noItems.charAt(i));
          Thread.sleep(20);
        }
        System.out.println();
        System.out.println();
     
    }else{
      System.out.println();
      for(int i = 0; i < youFound.length(); i++){
        System.out.printf("%c", youFound.charAt(i));
        Thread.sleep(20);
      }
      
      for(int i = 0; i < items.size(); i++){
        if(i == items.size() - 2){
          System.out.print(items.get(i).getName() + " and ");
        }
        else if(i == items.size() - 1){
          System.out.print(items.get(i).getName());
        }
        else{
          System.out.print(items.get(i).getName() + ", ");
        }
      }
      System.out.println();
      System.out.println();
     
    }
  }
  public Item remove(String itemName) {

    for (int i = items.size() - 1; i >= 0; i--) {

      Item item = items.get(i);

      if (item.getName().equals(itemName)) {
        return items.remove(i);
      }
    }
    return null;
  }

  public String toString() {
    String msg = "";

    for (Item item : items) {
      msg += item + ", ";
    }

    if (msg.length() > 0) {
      msg = msg.substring(msg.length() - 2);
    }
    return msg;
  }
}
