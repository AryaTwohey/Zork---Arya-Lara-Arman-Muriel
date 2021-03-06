package zork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game {

  public static HashMap<String, Room> roomMap = new HashMap<String, Room>();
  public static HashMap<String, Item> itemMap = new HashMap<String, Item>();
  private Parser parser;
  private Room currentRoom;
  Inventory playerInventory;
  Item item;
  String weapons[] = { "pistol", "bat", "ak 47", "pitchfork", "plastic spoon", "knife", "sword" };
  int playerHealth = 500; //total player health
  int playerXp = 0;   //total playerXP before accomplishing tasks
  //This ArrayList contains everything the player has done and picked up 
  TreeSet<String> all = new TreeSet<String>(); 

  int xpKeyCounter = 0; //insures the player only gets xp once for collecting all the keys

  public static final String yellow = "\u001B[33m"; // for the welcome message
  public static final String white = "\u001B[0m"; // also for the welcome message
  public static final String blue = "\u001B[34m"; // for quit message
  public static final String red = "\u001B[31m"; // for red coloured text (blood)
  public static final String green = "\u001B[32m"; //for green colour (Shreck)

  /**
   * Create the game and initialize its internal map.
   * 
   */
  public Game() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");
      initCharacters("src\\zork\\data\\characters.json");
      initWeapons("src\\zork\\data\\weapons.json");
      currentRoom = roomMap.get("Outside Entrance");

    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();

    /*
     * * * * * * * * * * * * * * * * * * * * * * * * *
     * 8633g - - > Sum of all items weights *
     * 4317 - - > Approximatley 50% of max capacity *
     * I (Arya) think that this should be the max- *
     * carrying capacity for a inventory *
     * * * * * * * * * * * * * * * * * * * * * * * * *
     */
    playerInventory = new Inventory(4317);
  }

  //Arya did reset
  private void reset() {
    try {
      initRooms("src\\zork\\data\\rooms.json");
      initItems("src\\zork\\data\\items.json");
      initCharacters("src\\zork\\data\\characters.json");
      initWeapons("src\\zork\\data\\weapons.json");
      currentRoom = roomMap.get("Outside Entrance");
    } catch (Exception e) {
      e.printStackTrace();
    }
    parser = new Parser();
    playerInventory = new Inventory(4317);  //half of sum of all items weight, player can only pick up 50% of items
    playerHealth = 500; 
    playerXp = 0;
    

  }

    /**
     * Muriel did initItems as well as items.json
     * initializes the items 
     * takes items and their characteristics in items.json and allows them to be used in our game
     * places the item into the room it belongs in
     */
  private void initItems(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String stringJson = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(stringJson);

    JSONArray itemsJson = (JSONArray) json.get("items");

    for (Object obj : itemsJson) {
      Item item = new Item();
      String itemName = (String) ((JSONObject) obj).get("name");
      String itemId = (String) ((JSONObject) obj).get("id");
      int itemWeight = Integer.parseInt((String) ((JSONObject) obj).get("Weight"));
      String itemDescription = (String) ((JSONObject) obj).get("description");
      String itemStartingRoom = (String) ((JSONObject) obj).get("starting location");
      String sIsNote = (String) ((JSONObject) obj).get("isNote");

      item.setName(itemName);
      item.setDescription(itemDescription);
      item.setWeight(itemWeight);
      if (sIsNote != null) {
        item.setIsNote(Boolean.parseBoolean(sIsNote));
      }
      roomMap.get(itemStartingRoom).addItem(item);

    }
  
  }

  /** 
   * Lara did the rooms.json 
   * initializes the rooms in the game, with their given attributes  from the rooms.json file and compiles it to play 
   * states the exists of the rooms and whether or not it's open or locked 
   * sets the name of the room
   * sets the exits of that room
   * sets the room id in the roomMap
  */

  private void initRooms(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);

    JSONArray jsonRooms = (JSONArray) json.get("rooms");

    for (Object roomObj : jsonRooms) {
      Room room = new Room();
      String roomName = (String) ((JSONObject) roomObj).get("name");
      String roomId = (String) ((JSONObject) roomObj).get("id");
      String roomDescription = (String) ((JSONObject) roomObj).get("description");
      room.setDescription(roomDescription);
      room.setRoomName(roomName);

      JSONArray jsonExits = (JSONArray) ((JSONObject) roomObj).get("exits");
      ArrayList<Exit> exits = new ArrayList<Exit>();
      for (Object exitObj : jsonExits) {
        String direction = (String) ((JSONObject) exitObj).get("direction");
        String adjacentRoom = (String) ((JSONObject) exitObj).get("adjacentRoom");
        String keyId = (String) ((JSONObject) exitObj).get("keyId");
        Boolean isLocked = (Boolean) ((JSONObject) exitObj).get("isLocked");
        Boolean isOpen = (Boolean) ((JSONObject) exitObj).get("isOpen");
        Exit exit = new Exit(direction, adjacentRoom, isLocked, keyId, isOpen);
        exits.add(exit);
      }

      room.setRoomName(roomName);
      room.setExits(exits);
      roomMap.put(roomId, room);
    }
  }

   /**
    * lara and muriel did this
   * initializes the characters to be used in the games
   * allows us to set their location, health etc. and use those characteristics and the actual character
   * places characters in their locations 
   */

  private void initCharacters(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String jsonString = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(jsonString);

    JSONArray jsonChar = (JSONArray) json.get("characters");

    for (Object charObj : jsonChar) {
      Character character = new Character();
      String charName = (String) ((JSONObject) charObj).get("name");
      String charId = (String) ((JSONObject) charObj).get("id");
      String charDescription = (String) ((JSONObject) charObj).get("description");
      String charLocation = (String) ((JSONObject) charObj).get("starting location");
      int charHealth = Integer.parseInt((String) ((JSONObject) charObj).get("Health"));
      int charDamage = Integer.parseInt((String) ((JSONObject) charObj).get("Damage"));

      character.setLocation(charLocation);
      character.setDescription(charDescription);
      character.setName(charName);
      character.setHealth(charHealth);
      character.setDamage(charDamage);
      roomMap.get(charLocation).addCharacter(character);
    }
  }


    
    
  
  
  /**
   * lara and muriel did this
   * initializes the weapons 
   * takes the weapon plus their attributes from the weapons.json
   * compiles them into our game in the respective location/rooms 
   * sets the name of the different weapons
   * sets the description of the weapon
   * sets how much damage that weapon can do 
   * sets the starting room of where that weapon will be when the game starts 
   */


  private void initWeapons(String fileName) throws Exception {
    Path path = Path.of(fileName);
    String stringJson = Files.readString(path);
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(stringJson);

    JSONArray weaponsJson = (JSONArray) json.get("weapons");

    for (Object weaponobj : weaponsJson) {
      Weapon weapon = new Weapon();
      String weaponName = (String) ((JSONObject) weaponobj).get("name");
      String weaponId = (String) ((JSONObject) weaponobj).get("id");
      int weaponWeight = Integer.parseInt((String) ((JSONObject) weaponobj).get("weight"));
      String weaponDescription = (String) ((JSONObject) weaponobj).get("description");
      String weaponStartingRoom = (String) ((JSONObject) weaponobj).get("starting location");
      Integer weaponDamage = Integer.parseInt((String) ((JSONObject) weaponobj).get("damage"));

      weapon.setName(weaponName);
      weapon.setDescription(weaponDescription);
      weapon.setWeight(weaponWeight);
      weapon.setDamage(weaponDamage);
      roomMap.get(weaponStartingRoom).addItem(weapon);

    }
  }
  

  /**
   * Required for printable messges, doesnt do anything else
   * 
   * @throws InterruptedException
   */

  public void play() throws InterruptedException {
    printWelcome();
    boolean finished = false;
    while (!finished) {
      Command command;
      try {
        command = parser.getCommand();
        finished = processCommand(command);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    credits();
    System.out.println();
    System.out.println();
    System.exit(0);
  }

  /**
   * Arya did this
   * Print out the opening message for the player.
   * 
   * @throws InterruptedException
   */
  private void printWelcome() throws InterruptedException {

    /**This is the welcome message a user gets when running the game
     * It uses System.out.printf and Thread.sleep to have a typewriter effect
     */

    String welcome = "Welcome To Death Doll!!!";
    String creators = "A text-adventure game created by Arya, Arman, Lara and Muriel!!!";
    String help = "Type 'help' to see the commands";
    String line = yellow + "________________________________________________________________" + white;


    System.out.println();
    System.out.println();

    for (int i = 0; i < line.length(); i++) {

      System.out.printf("%c", line.charAt(i));
      Thread.sleep(5);
    }
    System.out.println();
    System.out.println();

    for (int i = 0; i < welcome.length(); i++) {

      System.out.printf("%c", welcome.charAt(i));
      Thread.sleep(5);
    }

    System.out.println();
    System.out.println();

    for (int i = 0; i < creators.length(); i++) {

      System.out.printf("%c", creators.charAt(i));
      Thread.sleep(5);
    }

    System.out.println();
    System.out.println();

    for (int i = 0; i < help.length(); i++) {

      System.out.printf("%c", help.charAt(i));
      Thread.sleep(5);
    }

    System.out.println();
    System.out.println();

    for (int i = 0; i < line.length(); i++) {

      System.out.printf("%c", line.charAt(i));
      Thread.sleep(5);
    }
    System.out.println();
    System.out.println();

    /**
     * Hard coded to get text coloured in red
     */

    String firstScentence = "In front of you there is a large house with no lights on.";
    String secondScentence = "The windows are boarded up and you can hear squeaking and faint screaming.";
    String thirdScentence = "To the North of you there is an open door covered in ";
    String blood = red + "blood" + white + "...";
    String exits = "Exits: North ";

    System.out.println(firstScentence);
    System.out.println(secondScentence);
    System.out.print(thirdScentence);
    System.out.print(blood);
    System.out.println();
    System.out.println();
    System.out.println(exits);
    System.out.println();
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends
   * the game, true is returned, otherwise false is returned.
   * 
   * @throws InterruptedException this is for the printhelp message, dont worry
   *                              it doenst do anything bad
   */
  private boolean processCommand(Command command) throws InterruptedException {

    //if it doesn't understand the command it will simply return false and exit processCommand
    if (command.isUnknown()) {

      System.out.println();
      System.out.println("I don't know what you mean...");
      return false;
    }

    //grabs the commandWord from the comand the user inputted 
    String commandWord = command.getCommandWord();

    //through these if statements it checks whether the commandWord matches any of these and will do a specific method based on which command word it matches
    if (commandWord.equals("help")){
      printHelp();
    }else if (commandWord.equals("go") || commandWord.equals("move") || commandWord.equals("run")){
      goRoom(command);
    } else if (commandWord.equals("quit")) {
    quit();
    System.out.println();
    System.out.println();
    System.exit(0);
    } else if (commandWord.equals("eat")) {
      System.out.println();
      System.out.println("Eat?!? Are you serious?!");
    } else if (commandWord.equals("take")) {
      takeItem(command);
      //gives player xp for collecting all keys - only does this once
      if(playerInventory.hasAllKeys() && xpKeyCounter == 0){
        System.out.println();
        System.out.println("You now have all the keys - here's some xp");
        xpKeyCounter++; 
        playerXp += 75; 
        System.out.println();
        System.out.println(blue + "PLAYER XP + 75" + white);
      }
    } else if (commandWord.equals("drop")) {
      dropItem(command);
    } else if(commandWord.equals("heal") || commandWord.equals("restore")){
      heal(); 
    } else if (commandWord.equals("kill") || commandWord.equals("shoot") || commandWord.equals("fire")
        || commandWord.equals("hit") || commandWord.equals("stab") || commandWord.equals("use")) {
      System.out.println();
      attack(command);
      System.out.println(currentRoom.exits());
      System.out.println();
    } else if (commandWord.equals("search")) {
      System.out.println();
      search(command);
      System.out.println(currentRoom.exits());
      System.out.println();
    } else if (commandWord.equals("read")) {
      System.out.println();
      read(command);
      System.out.println(currentRoom.exits());
      System.out.println();
    } else if (commandWord.equals("inventory") || commandWord.equals("display")) {
      displayInventory(commandWord);
    } else if (commandWord.equals("space")) {
      inventorySpace(command);
      System.out.println(currentRoom.exits());
    
    }else if (commandWord.equals("inspect")) {
      inspect(command);
    } else if (commandWord.equals("restart") || commandWord.equals("reset")) {

      System.out.println();
      System.out.println("Your game is being reset...");
      reset();

      String welcome = "Welcome To Death Doll!!!";
      String creators = "A text-adventure game created by Arya, Arman, Lara and Muriel!!!";
      String help = "Type 'help' to see the commands";
      String line = yellow + "________________________________________________________________" + white;

      // Cool Printable Message

      System.out.println();
      System.out.println();
      // line

      for (int i = 0; i < line.length(); i++) {

        System.out.printf("%c", line.charAt(i));
        Thread.sleep(5);
      }
      System.out.println();
      System.out.println();

      for (int i = 0; i < welcome.length(); i++) {

        System.out.printf("%c", welcome.charAt(i));
        Thread.sleep(5);
      }

      System.out.println();
      System.out.println();

      for (int i = 0; i < creators.length(); i++) {

        System.out.printf("%c", creators.charAt(i));
        Thread.sleep(5);
      }

      System.out.println();
      System.out.println();

      for (int i = 0; i < help.length(); i++) {

        System.out.printf("%c", help.charAt(i));
        Thread.sleep(5);
      }

      System.out.println();
      System.out.println();

      for (int i = 0; i < line.length(); i++) {

        System.out.printf("%c", line.charAt(i));
        Thread.sleep(5);
      }

      System.out.println();
      System.out.println();

      /**
       * Sorry for all the strings
       * I just wanted to have the "blood" text coloured in red
       */

      String firstScentence = "In front of you there is a large house with no lights on.";
      String secondScentence = "The windows are boarded up and you can hear squeaking and faint screaming.";
      String thirdScentence = "To the North of you there is an open door covered in ";
      String forthScentence = "...the ";
      String fifthScentence = " of your friend.";
      String blood = red + "blood" + white;
      String exits = "Exits: North ";

      System.out.println(firstScentence);
      System.out.println(secondScentence);
      System.out.print(thirdScentence);
      System.out.print(blood);
      System.out.print(forthScentence);
      System.out.print(blood);
      System.out.println(fifthScentence);
      System.out.println();
      System.out.println(exits);
      System.out.println();
    }

    /**
     * Arman and Arya did this 
     * if the command word is fred
     * it will print out fred is hot 
     * and give the player 500xp (simple easter egg)
     * it will only give the player the xp once as it adds the word "fred" to a treeSet 
     * this makes sure that the player only receives xp once as it will only reward the player xp if fred does NOT exist in the treeSet
     */

     //Hidden Commands that give players extra xp
    else if (commandWord.equals("fred")) {
      System.out.println();
      System.out.println();

      String importantMessage = "Fred is hot";

      for (int i = 0; i < importantMessage.length(); i++) {

        System.out.printf("%c", importantMessage.charAt(i));
        Thread.sleep(500);

      }
      System.out.println();
      if(!all.contains("fred")){
        System.out.println("Fred is hot, but you are not, here is some XP in return - you can only do this once");
        playerXp += 500; 
        System.out.println(blue + "PLAYER XP + 500" + white); 
      }
      all.add("fred");
    } 
    /**
     * Arman and Arya did this
     * if the command word is winson
     * it will print out a famous Mr. Winson quote  
     * and give the player 500xp (simple easter egg)
     * it will only give the player the xp once as it adds the word "winson" to a treeSet 
     * this makes sure that the player only receives xp once as it will only reward the player xp if fred does NOT exist in the treeSet
     */
    else if (commandWord.equals("winson")) {

      int ran = (int) (Math.random() * 2);
      if (ran == 0) {
        String quotes = "''";

        System.out.println();
        System.out.print("Coincidence: ");
        System.out.println(quotes
            + "A situation in which events happen at the same in a way that is not planned or expected." + quotes);
        System.out.println();
        System.out.println("...however, I think math");
        System.out.println();
        System.out.println("    -Greg Winson 2011-2022");
      } else {
        System.out.println();
        System.out.println("Remember, the most common mistakes in Math is arithmetic involving negative numbers.");
        System.out.println();
        System.out.println("     -Greg Winson 2011-2022");
      }

      System.out.println();
      if(!all.contains("winson")){
        playerXp += 500; 
        System.out.println("You found the secret XP - you can only do this once \n - Mr. Deslauriers Best Friend (You know who it is)"); 
        System.out.println(blue + "PLAYER XP + 500" + white);
      }
      all.add("winson");
    }
    if(currentRoom.getRoomName().equals("Cellar")){
      if(currentRoom.noMoreEnemies()){
        playerXp += 10000; 
        System.out.println(blue + "Won the game! - PLAYER XP + 10000" + white);
        return true;
      }
    }
    return false;
  }

  // implementations of user commands:

  /**
   * Arman did this method
   * This is a void method that allows the player to heal when fighting enemies
   * This method uses a scanner to keep track of what the player is requesting 
   * It asks the player what heal option they would like and the player types their response 
   * If the response is not understood it prompts the player to call heal again if they would like to restore their health 
   * If the player has requested a certain type of heal (eg. full restore) but does not have enough xp to purchase it, they will not be able to purchase it 
   * Once the purchase is confirmed xp corresponding with the price of the item will be removed and the player's health and xp will be displayed
   */
  private void heal() {
    Scanner in = new Scanner(System.in); //Use of scanner to get user input 
    
    System.out.println();
    System.out.println("Heal buying options: full restore (Price: 200xp), half restore (Price: 150xp), 100 health (Price: 75xp)");  //display options
    System.out.print("What do you want: ");
    String option = in.nextLine().toLowerCase();
    if(option.equals("full") || option.equals("full restore")){ //if player types full restore, HP is automatically set to 500
      if(playerXp - 200 < 0){   //Check to see if player has enough XP for full restore
        System.out.println("You do not have enough xp for this purchase");
        System.out.println("200xp needed and you have " + playerXp + " xp");
      }
      else{
        playerXp -= 200;  //If they meet the XP requirement, subtrace the cost of full restore from their playerXP
        System.out.println("Purchase Confirmed: Price 200xp");
        playerHealth = 500;
        System.out.println(green + "current health: " + playerHealth + white);    //display currentHealth after healing
        System.out.println(blue + "current xp :" + playerXp + white);   //display current XP 
      }
      
    }   
    /**These if statements follow the exact same
     * Procedures as "full restore", but instead
     * of healing to 500 HP, 250HP is added to health 
     * instead, hence the "half-restore"
     */
    else if(option.equals("half") || option.equals("half restore")){
      if(playerXp - 150 < 0){
        System.out.println("You do not have enough xp for this purchase");
        System.out.println("150xp needed and you have " + playerXp + " xp");
      }
      else{
        playerXp -= 150; 
        System.out.println("Purchase Confirmed: Price 150xp");
        playerHealth += 250;
        if(playerHealth > 500){
          playerHealth = 500; 
        } 
        System.out.println(green + "current health: " + playerHealth + white);
        System.out.println(blue + "current xp :" + playerXp + white);
      }
    } 
    else if(option.equals("100") || option.equals("100 health")){
      if(playerXp - 75 < 0){
        System.out.println("You do not have enough xp for this purchase");
        System.out.println("75xp needed and you have " + playerXp + " xp");
      }
      else{
        playerXp -= 75; 
        System.out.println("Purchase Confirmed: Price 75xp");
        playerHealth += 100;
        if(playerHealth > 500){
          playerHealth = 500; 
        } 
        System.out.println(green + "current health: " + playerHealth + white);
        System.out.println(blue + "current xp :" + playerXp + white);
      }
    } 
    else if(option.equals("cancel")){ //If a player decides that they do not wan't to spend XP, they hit cancel 
      System.out.println();
      System.out.println("You left the shop");
      System.out.println();
    }
    else{
      System.out.println();
      System.out.println("Sorry didn't understand that - call heal again if you still want to purchase meds");
      System.out.println();
    }
    currentRoom.exits(); //display the exits, so the user does not have to scroll up a few lines to find them
  }
  /**
   * Arya did this method
   * This is a quit method that prints a quit message, 
   * before System.exit terminates the compiler */
  private void quit() throws InterruptedException {
    String quit = blue + "Thank you for playing. Good Bye." + white;
    String threeDots = "...";
    System.out.println();
    System.out.println();
    System.out.println();
    for (int i = 0; i < quit.length(); i++) {
      System.out.printf("%c", quit.charAt(i));
      Thread.sleep(15);
    }
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.print("TERMINATING COMPILER");

    for (int i = 0; i < threeDots.length(); i++) {

      System.out.printf("%c", threeDots.charAt(i));
      Thread.sleep(1500);
    }

    System.out.println();

    System.out.println("TERMINATION COMPLETE");
    System.out.println();
  }

  /**
   * insepct method
   * Arya worked on this - Arman helped
   * inspects elements - giving description or what item does/contains
   */
  private void inspect(Command command){
  if(!command.hasExtraWords()){

    System.out.println();
    System.out.println("Inspect What?");
    System.out.println();

  }else{
    String itemName; 
    if (command.getExtraWords().size() > 1) {
      String first = command.getExtraWords().get(0);
      String second = command.getExtraWords().get(1);
      itemName = first + " " + second;
    } else {
      itemName = command.getExtraWords().get(0);
    }
    System.out.println();
    System.out.println(currentRoom.inspectItem(itemName));
    System.out.println();
  }
}
  /**
   * Arman did this method - Arya helped
   * this method is used for attacking enemies in the rooms
   * @param command the players command
   * @throws InterruptedException for quit method
   */
  private void attack(Command command) throws InterruptedException {
    if (currentRoom.hasEnemy()) {
      if (!command.hasExtraWords()) {
        System.out.println();
        System.out.println("I need more information");
        System.out.println();
      } else {
        /**
         * creates weapon name 
         * either by getting 1st and 2nd word of the array (2 word weapon) 
         * or getting the 1st word of array (1 word weapon)
         */
        String weaponName;
        if (command.getExtraWords().size() > 1) {
          String first = command.getExtraWords().get(0);
          String second = command.getExtraWords().get(1);
          weaponName = first + " " + second;
        } else {
          weaponName = command.getExtraWords().get(0);
        }
        /**
         * checks if the weapon the player inputted is a valid weapon and if it is in the player's inventory
         * subtracts the weapon's damage from the enemy's health 
         * subtracts the enemy's damage from the player's health 
         * displays message displaying the damage done, the damage taken, and a quote
         */
        if (validWeapon(weaponName) && playerInventory.inInventory(weaponName)) {
          Weapon weapon = (Weapon) playerInventory.findItem(weaponName);

          currentRoom.getCharacter().setHealth(currentRoom.getCharacter().getHealth() - weapon.getDamage());
          playerHealth -= currentRoom.getCharacter().getDamage();

          System.out.println("You did " + weapon.getDamage() + " damage on " + currentRoom.getCharacter().getName()
              + " they did " + currentRoom.getCharacter().getDamage() + " damage to you, your health is now "
              + playerHealth + " and " + currentRoom.getCharacter().getName() + " says ''" + quotes() + "''");

          //if the player has no health calls playerDead method
          if (playerHealth <= 0) {
            playerDead();
          }
          //if the enemy has no more health calls enemyDead method
          if (currentRoom.getCharacter().getHealth() <= 0) {
            killedEnemy();
          }
        } else {
          if (!validWeapon(weaponName)) {
            System.out.println();
            System.out.println(weaponName + " is not a valid weapon.");
            System.out.println();
          } else {
            System.out.println();
            System.out.println(weaponName + " is not in your inventory.");
            System.out.println();
          }
        }
      }
      /**
      * if it does not satisfy the if statement
      * print - is not a valid weapon if it is not a valid weapon
      * print - is not in your inventory if it is not in the player's inventory 
      */
    } else {
      System.out.println("There is nothing to attack/kill in this space.");
    }
  }
  /**
   * Arman did this method
   * this method is called when the health of the enemy that the player was attacking becomes 0
   * first it prints out that the player has defeated the enemy and displays the enemy's name 
   * removes the enemy from the room 
   * adds 100xp to the player
   * explaining if statements below 
   */
  private void killedEnemy() {
    System.out.println();
              System.out.println("You have defeated " + currentRoom.getCharacter().getName());
    System.out.println();
    currentRoom.removeCharacter();
    System.out.println();

    playerXp += 100;
    System.out.println(blue + "Enemy defeated - PLAYER XP + 100" + white);

    /**
     * if the player is in the cellar, there is enemy's left and the enemy's name is Shreck 
     * -- it will print out the character's description in green (for Shreck)
     * else if the player is in the cellar and the next enemy is not Shreck 
     * -- it will print out the character's description in white
     * else if the player was not in the cellar this means that they were in bedroom 4
     * -- it rewards the player with the 3d key, adding it to their inventory
     * -- if the player has all the keys it will give them 75xp 
     * -- if the player does not have all keys it will tell them to go find the other keys
     */
    if (currentRoom.getCharacter() != null && currentRoom.getCharacter().getName().equals("Shreck")
        && currentRoom.getRoomName().equals("Cellar")) {
        System.out.println(green + currentRoom.getCharacter().getDescription() + white);
    } else if (currentRoom.getRoomName().equals("Cellar") && currentRoom.getCharacter() != null
        && currentRoom.getCharacter().getName() != "Shreck") {
        System.out.println(currentRoom.getCharacter().getDescription());

    } else if (!currentRoom.getRoomName().equals("Cellar")) {
        Item key3 = new Item(50, "key 3", false,
            "Congratulations on finding the last key, but don't celebrate just yet. Head down to the cellar to figure out what's next.");
        playerInventory.add(key3);
        System.out.println();
        if(playerInventory.hasAllKeys()){
          System.out.println("You now receive key 3, here's some xp for finding all keys");
          if (!all.contains(key3.getName())) {
            playerXp += 75;
            System.out.println(blue + "PLAYER XP + 75" + white);
            xpKeyCounter++; 
          }
          all.add(key3.getName());
        }
        else{
          System.out.println("You now receive key 3, go find the missing key(s) to move on");
        }
    }
  }
  /**
   * Arman did this method
   * prints out a death message and then quits the game
   * @throws InterruptedException for the quit method
   */
  private void playerDead() throws InterruptedException {
    System.out.println();
    System.out.println(
        "_______________________________________________________________________________________________________________________________________");
    System.out.println();
    System.out.println(red + " YOU DIED, better luck next time..." + white);
    System.out.println();
    System.out.println(blue + "''Sometimes, the things you see in the shadows are more than just shadows.''" + white);
    quit();
    System.out.println();
    System.exit(0);
    System.out.println();
  }

  /**
   * Arman did this method
   * @param weaponName takes in a weaponName
   * searches through the array of weapons to see if the weaponName exists there
   * @return true if it is a valid weapon
   * @return false if it is not a valid weapon
   */
  private boolean validWeapon(String weaponName) {
    for (String s : weapons) {
      if (s.equals(weaponName)) {
        return true;
      }
    }
    return false;
  }
  /**
   * Arman did this method
   * @param command take in the command that the player inputted
   * if the command has only one word then we don't know what to read 
   * if the command does have extra words we go do what's in the else statement 
   * if there is more than one extra word in the command we know that what we want to read is potentially more than one word long 
   * so we grab the first two extra words and make them into one string - itemName 
   * else we know that what we want to read is only one word long so we set the item name to the first index of extraWords
   * if the item that the user inputted is not in their inventory (check using inInventory) then we cannot read it 
   * else we can read it and the method prints out the items description
   */
  private void read(Command command) {
    if (!command.hasExtraWords()) {
      System.out.println("read what?");
    } else {
      String itemName;

      if (command.getExtraWords().size() > 1) {
        String first = command.getExtraWords().get(0);
        String second = command.getExtraWords().get(1);
        itemName = first + " " + second;
      } else {
        itemName = command.getExtraWords().get(0);
      }

      if (!playerInventory.inInventory(itemName)) {
        System.out.println();
        System.out.println("I can't read " + itemName + " because it is not in your inventory");
        System.out.println();
      } else {
        String description = playerInventory.readItem(itemName);
        System.out.println();
        System.out.println(description);
        System.out.println();
      }
    }
  }

  //Arya did this method - display space
  private void inventorySpace(Command command) {
    playerInventory.inventorySpace();
  }
  //Arya did this method - searches room and displays items
  private void search(Command command) throws InterruptedException {
    currentRoom.search();
  }
  /**
   * Arya did this 
   * Print out some help information and a list of the command words.
   * 
   * @throws InterruptedException // this is for the printed message it doesnt do
   *                              anything
   */
 
  private void printHelp() throws InterruptedException {
    String helperMessage = "Your command words are below, use them to win the game ";

    System.out.println();
    System.out.println();

    for (int i = 0; i < helperMessage.length(); i++) {

      System.out.printf("%c", helperMessage.charAt(i));

      Thread.sleep(15);
    }
    System.out.println();
    System.out.println();

    parser.showCommands();

    System.out.println();
    System.out.println();

  }

  //Arya and Arman did this
  //displays the players inventory as well as their xp and health 
  public void displayInventory(String commandWord) throws InterruptedException {
    playerInventory.displayInventory();
    if(commandWord.equals("display")){
      System.out.println();
      System.out.println(blue + "player xp: " + playerXp);
      System.out.println();
      System.out.println(green + "player health: " + playerHealth);
    }
  }

  //Arya did this Arman helped
  private void takeItem(Command command) {

    if (!command.hasExtraWords()) {
      System.out.println();
      System.out.println("Take What?");   //Without extra words, "take what"
      System.out.println();
    } else {
      String itemName;    //create a variable called itemName 
      if (command.getExtraWords().size() > 1) {
        String first = command.getExtraWords().get(0);
        String second = command.getExtraWords().get(1);
        itemName = first + " " + second;
      } else {
        itemName = command.getExtraWords().get(0);  //grabs the extra words at the first and second index of itemName
      }

      Item item = currentRoom.removeItem(itemName); //Remove the item from the current room
      if (item == null) {       //if the currentRoom.getItem returns null then prompt the user that there is no item in the room
        System.out.println();
        System.out.print("This " + itemName + " does not exist in this room. ");  
        System.out.println("Or try typing " + blue + "search " + white + "to find the proper name of the item");
      } else {
        if (!playerInventory.add(item)) //Used for XP system after taking item once, add it to a seprate arrayList
          currentRoom.addItem(item);  //add the item to the room 
        else {

          //Weapons have different effects on the XP functionality

          System.out.println();
          if(!all.contains(item.getName()) && !validWeapon(item.getName())){  //if the item is not a weapon and is not in the all<> arrayList, then award points
            playerXp += 15; 
            System.out.println(blue + "Item picked up -> PLAYER XP + 15" + white);  //show the player their added XP 
          }
          if(!all.contains(item.getName()) && validWeapon(item.getName())){ //if the item is a weapons and does not exist in all<> arraylist, then remove 50 XP
            if(playerXp - 50 < 0){  //player cannot take weapon if they do not have at least 50 XP 
              System.out.println("You do not have enough xp to take this weapon");  
              playerInventory.remove(itemName); 
              currentRoom.addItem(item); 
            }
            else{
              playerXp -= 50; //subtrace 50 XP from inventory
              System.out.println(red + "Weapon picked up -> PLAYER XP - 50" + white); //tell the user that they have picked up the weapon
            } 
          }
          all.add(item.getName());  //add the weapon to the all<> list
          System.out.println("You took the " + itemName);
        }
      }
      System.out.print(currentRoom.exits());  //display room exists
      System.out.println();
    }
  }

  //Arya did this - Arman helped
  private void dropItem(Command command) {

    if (!command.hasExtraWords()) {
      System.out.println();
      System.out.println("Drop What?"); //if the command has no extra words, display message
      System.out.println();
    } else {
      String itemName;
      if (command.getExtraWords().size() > 1) { //this is used for multiple word items
        String first = command.getExtraWords().get(0);  //first index of first word
        String second = command.getExtraWords().get(1); //first index of second word
        itemName = first + " " + second;
      } else {
        itemName = command.getExtraWords().get(0);  //grab the first index of the extraWords
      }

      Item item = playerInventory.remove(itemName);
      if (item == null) { //check to see if item is actaully in players inventory
        System.out.println();
        System.out.println("You don't have " + itemName + " in your inventory");  //show the uses that they do not have this ite
        System.out.println();
      } else {
        currentRoom.addItem(item);  //if they do have this item, then add it to the currentRooms inventory
        System.out.println();
        System.out.println("You dropped the " + itemName);  //display the weapon that was dropped
      }
      System.out.println(currentRoom.exits());  //display room exits
      System.out.println();
    }
  }

  /**
   * Arman and Arya made some changes to this
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */
  private void goRoom(Command command) {
    if (!command.hasExtraWords()) {
      // if there are no second word, we don't know where to go...
      System.out.println();
      System.out.println("Go where?");
      return;
    }

    ArrayList<String> rest = command.getExtraWords();

    String direction = rest.get(0);

    // Try to leave current room.
    Room nextRoom = currentRoom.nextRoom(direction, playerInventory.hasAllKeys());

    if (nextRoom == null) {
      System.out.println();
      System.out.println("Can't go there!");
    } else {
      currentRoom = nextRoom;
      if(!all.contains(currentRoom.getRoomName())){
        playerXp += 5; 
        System.out.println(blue + "Explored a new room - PLAYER XP + 5" + white);
      }
      all.add(currentRoom.getRoomName());
      System.out.println(currentRoom.longDescription());
      if(nextRoom.getRoomName().equals("Cellar")){
        System.out.println(currentRoom.getCharacter().getDescription()); 
      }
    }
  }

 
  /**
   * Arman did this
   * created a long ArrayList of different famous quotes from horror movies 
   * generates a random number between 0 and the size of the ArrayList of quotes
   * @return a quote from the ArrayList of strings using the random number
   */
  private String quotes() {
    ArrayList<String> quotes = new ArrayList<String>(Arrays.asList("Be afraid. Be very afraid.", "We all go a little mad sometimes.", "To a new world of gods and monsters.", 
    "If you had learned a little from me, you would not beg to live.", "If you wish to see strange things, I have the power to show them to you.", "Thrill me!", 
    "I guess everyone's entitled to one good scare", "Don't look back.", "Movies don't create psychos; movies make psychos more creative.", 
    "When there is no more room in hell, the dead will walk the earth.", "Swallow This.", "You want to see pain? I can show you.", 
    "I see dead people", "Do you want to play a game.", "That was a mistake...", "Here's Johnny!", "Who do you think you are.", "You think you know suffering?", 
    "I will show you pain in ways you couldn't even imagine.", "You can scream all you want, it won't save you.", "Your not ready for what's next.", 
    "You've made it too far, I'm going to put an end to that.", "Wanna play?", "I'm gonna put you to sleep", "You look delicious", 
    "I'm every nightmare you've ever had", "I'm your worst dream come true.", "I'm everything you ever were afraid of.", "Wendy, I'm home!", "I'm a messenger of God", 
    "You're doomed if you stay here!", "This place is cursed.", "Do you think I fear you!!", "You don't know fear like I do", "Have you ever been this scared?",
    "If you kill me, your no worse of a person than I am", "This society shuns people like us.", "Why so serious", "I'm a symbol of fear", "we're just getting started", 
    "There's nothing to fear, except for me", "I'm intrigued to see how much we can make you bleed", "And you thought ghosts weren't real."));

    int random = (int) (Math.random() * quotes.size()); 
    return quotes.get(random); 
  }

  //Arya did this
  private void credits() throws InterruptedException {

    /**Method that is only called when the player has won
     * Has hard-coded strings to create printable message 
     * Uses System.out.printf and Thread.sleep to acheive this effect
     */

    String dots = "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *";
    String thankYouMessage = "                                                CONGRATULATIONS YOU HAVE ESCAPED DEATH DOLL";
    String friend = "                                                     You and your friend are now free";
    String creators = "A text-adventure game created by Arya, Arman, Lara & Muriel";
    
   
  
    System.out.println();
    System.out.println();

    for (int i = 0; i < dots.length(); i++) {
      System.out.printf("%c", dots.charAt(i));
      Thread.sleep(5);
    }
    System.out.println();
    System.out.println();

    for (int i = 0; i < thankYouMessage.length(); i++) {

      System.out.printf("%c", thankYouMessage.charAt(i));
      Thread.sleep(20);
    }
    System.out.println();
    System.out.println();

    for (int i = 0; i < friend.length(); i++) {

      System.out.printf("%c", friend.charAt(i));
      Thread.sleep(20);
    }
    System.out.println();
    System.out.println();

    for (int i = 0; i < dots.length(); i++) {
      System.out.printf("%c", dots.charAt(i));
      Thread.sleep(5);
    }
    System.out.println();
    System.out.println();
    System.out.println();

    for (int i = 0; i < creators.length(); i++) {

      System.out.printf("%c", creators.charAt(i));
      Thread.sleep(20);
    }
    System.out.println();
    System.out.println();
    
    String displayXP = blue + "XP COLLECTED: " + String.valueOf(white + yellow + playerXp + white);
    System.out.println();
    System.out.println();

    for(int i = 0; i < displayXP.length(); i++){
      System.out.printf("%c", displayXP.charAt(i));
      Thread.sleep(20);
    }
    System.out.println();
    System.out.println();
  }

}
