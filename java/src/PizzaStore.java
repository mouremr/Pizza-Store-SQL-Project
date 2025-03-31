/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql, authorisedUser); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql, authorisedUser); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql, authorisedUser); break;
                   case 10: updateMenu(esql, authorisedUser); break;
                   case 11: updateUser(esql, authorisedUser); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      String login = "";
      String password = "";
      int phonenum = 0;
      
      try{
         System.out.println("Enter login: ");
         login = in.readLine();
         System.out.println("Enter Password: ");
         password = in.readLine();
         System.out.println("Enter Phonenum: ");
         phonenum = Integer.parseInt(in.readLine());
      } catch(Exception e){
         System.out.println("Your input is invalid!");
      }
      try{
         esql.executeUpdate("INSERT INTO Users VALUES(\'" + login + "\', \'" + password + "\', \'Customer\', \'\', " + phonenum+");");
      } catch(Exception e){
         System.out.println("Error: " + e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      String login = "";
      String password = "";
      while(login == ""){
         try{
            System.out.println("Enter login: ");
            login = in.readLine();
            System.out.println("Enter Password: ");
            password = in.readLine();
         } catch(Exception e){
            System.out.println("Your input is invalid!");
         }
      }  
      try{
         List<List<String>> result = esql.executeQueryAndReturnResult("SELECT login FROM Users WHERE login = \'" + login + "\' and password = \'"+ password + "\';");
         return(result.get(0).get(0));
      } catch(Exception e){
         System.out.println(e.getMessage());
         return(null);
      }
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String login) {
      try {
         String query = "SELECT * FROM Users WHERE login='" + login + "'";
         List<List<String>> profileitems = null;
         profileitems = esql.executeQueryAndReturnResult(query);

         String[] headers = {"login:", "password:", "role:", "favoriteitems:", "phonenum:"};

         printformatted(headers, profileitems);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateProfile(PizzaStore esql, String login) {
      try {
         System.out.println("1. Update phone num");
         System.out.println("2. Update password");
         System.out.println("3. Update favorite item");
         switch(readChoice()){
            case 1: updatephonenum(esql, login); break;
            case 2: updatepassword(esql, login); break;
            case 3: updatefavoriteitem(esql, login); break;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updatephonenum(PizzaStore esql, String login){   
      try{   
         System.out.print("Enter new phone number: ");
         String newPhone = in.readLine();
         String query = "UPDATE Users SET phoneNum='" + newPhone + "' WHERE login='" + login + "'";
         esql.executeUpdate(query);
         System.out.println("Profile updated successfully.");
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void updatepassword(PizzaStore esql, String login){
      try{
         System.out.print("Enter new password: ");
         String newpassword = in.readLine();
         String query = "UPDATE Users SET password ='" + newpassword + "' WHERE login='" + login + "'";
         esql.executeUpdate(query);
         System.out.println("Profile updated successfully.");
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void updatefavoriteitem(PizzaStore esql, String login){
      try{
         System.out.print("Enter new favorite item: ");
         String newitem = in.readLine();
         String query = "UPDATE Users SET favoriteItems ='" + newitem.replace("'", "''") + "' WHERE login='" + login + "'";
         esql.executeUpdate(query);
         System.out.println("Profile updated successfully.");
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   


   public static void viewMenu(PizzaStore esql) {
      try {
         String order = "ASC";
         boolean viewing = true;
         
         while(viewing){
            System.out.println("1. View full Menu");
            System.out.println("2. Filter Price");
            System.out.println("3. Filter type");
            System.out.println("4. Flip order");
            System.out.println("5. Stop viewing");
            switch(readChoice()){
               case 1: viewall(esql, order); break;
               case 2: filterprice(esql, order); break;
               case 3: filtertype(esql, order); break;
               case 4: 
                  if(order.equals("ASC")){
                     order = "DESC";
                  } else{
                     order = "ASC";
                  }
                  break;
               case 5: viewing = false; break;
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewall(PizzaStore esql, String order){
      try{
         String query = "SELECT * FROM Items ORDER BY price "+ order;
         List<List<String>> items = null;
         items = esql.executeQueryAndReturnResult(query);
         

         String[] headers = {"itemname:", "ingredients:", "typeofitem:", "price:", "description?:"};

         printformatted(headers, items);
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void filterprice(PizzaStore esql, String order){
      try{
         System.out.println("Enter price: ");
         String price = in.readLine();
         List<List<String>> items = null;
         items = esql.executeQueryAndReturnResult("SELECT * FROM Items WHERE price <= " + price + " ORDER BY price " + order + ";");

         String[] headers = {"itemname:", "ingredients:", "typeofitem:", "price:", "description?:"};
         
         printformatted(headers, items);
      } catch(Exception e){
         System.out.println("Enter valid price!");
      }
   }
   public static void filtertype(PizzaStore esql, String order){
      try{
         System.out.println("Enter item type: ");
         String type = in.readLine();
         List<List<String>> items = null;
         items = esql.executeQueryAndReturnResult("SELECT * FROM Items WHERE TRIM(typeOfItem) = \'" + type + "\' ORDER BY price " + order + ";");

         String[] headers = {"itemname:", "ingredients:", "typeofitem:", "price:", "description?:"};

         printformatted(headers, items);
      } catch(Exception e){
         System.out.println("Enter valid item type!");
      }
   }

   public static void placeOrder(PizzaStore esql, String login) {
      try {
         System.out.print("Enter store ID: ");
         int storeID = Integer.parseInt(in.readLine().trim());
         boolean ordering = true;
         String itemName = "";
         int quantity = 0;
         
         List<String> itemNames = new ArrayList<String>();
         List<Integer> quantities = new ArrayList<Integer>();
         List<Double> prices = new ArrayList<Double>();
         double totalPrice = 0.0;

         while(ordering){  
            System.out.print("Enter item name or enter 1 to stop ordering: ");
            itemName = in.readLine().trim().replace("'", "''");
            
            if(itemName.equals("1")){
               ordering = false;
               break;
            }

            System.out.print("Enter quantity: ");
            quantity = Integer.parseInt(in.readLine().trim());

            if (itemName.isEmpty() || quantity <= 0) {
               System.out.println("Invalid input. Please check your values.");
               return;
            } else{
               quantities.add(quantity);
               itemNames.add(itemName);
            }
         }

         

         // Get item price
         for (int i = 0; i < itemNames.size(); i++) {
            String priceQuery = "SELECT price FROM Items WHERE itemName = '" + itemNames.get(i) + "'";
            List<List<String>> priceResult = esql.executeQueryAndReturnResult(priceQuery);
            double price = Double.parseDouble(priceResult.get(0).get(0));
            if (priceResult.isEmpty()) {
                  System.out.println(itemNames.get(i) + " not found, skipping item.");
                  prices.add(0.0);
            } else{
               prices.add(price);
            }
            totalPrice += prices.get(i) * quantities.get(i);
         }

         System.out.println(totalPrice);

         
         

         //Insert Order and get the new order ID

         // Get the last inserted order ID
         String orderIDQuery = "SELECT orderID FROM FoodOrder ORDER BY orderID DESC LIMIT 1";
         List<List<String>> orderIDResult = esql.executeQueryAndReturnResult(orderIDQuery);

         if (orderIDResult.isEmpty()) {
               System.out.println("Order placement failed.");
               return;
         }

         int orderID = Integer.parseInt(orderIDResult.get(0).get(0)) + 1;
         System.out.println(orderID);

         String orderQuery = "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) " + "VALUES (\'" + orderID + "\', '" + login + "', " + storeID + ", " + totalPrice + ", NOW(), 'incompete')";
         esql.executeUpdate(orderQuery);

         //Insert Items in Order
         for(int i = 0; i < itemNames.size(); i++){
            System.out.println();
            String itemsQuery = "INSERT INTO ItemsInOrder (orderID, itemName, quantity) " + "VALUES (" + orderID + ", '" + itemNames.get(i) + "', " + quantities.get(i) + ")";
            esql.executeUpdate(itemsQuery);
         }
         

         System.out.println("Order placed successfully! Your Order ID is: " + orderID);
         System.out.println("Total price: " + totalPrice);
      } catch (Exception e) {
         System.out.println("Error placing order: " + e.getMessage());
      }
   }

   public static void viewAllOrders(PizzaStore esql, String login) {
      try {

         String roleResult = returnrole(esql, login);

         String query;
         
         if (roleResult.trim().equals("manager") || roleResult.trim().equals("driver")) {
               query = "SELECT * FROM FoodOrder ORDER BY orderTimestamp DESC";
         } else {
               query = "SELECT * FROM FoodOrder WHERE TRIM(login) = \'" + login + "\' ORDER BY orderTimestamp DESC";
         }

         String[] headers = {"OrderID:", "placed by:", "storeID:", "totalPrice:", "orderTimestamp:", "orderStatus:"};
         
         
         List<List<String>> orders = esql.executeQueryAndReturnResult(query);
            
         printformatted(headers, orders);
            


       } catch (Exception e) {
           System.err.println("Error retrieving orders: " + e.getMessage());
       }
   }

   public static void viewRecentOrders(PizzaStore esql, String login) {
      try {
         String roleResult = returnrole(esql, login);
         String query = "";
         if (roleResult.trim().equals("manager") || roleResult.trim().equals("driver")) {
               query = "SELECT * FROM FoodOrder ORDER BY orderTimestamp DESC LIMIT 5";
         } else {
               query = "SELECT * FROM FoodOrder WHERE TRIM(login) = \'" + login + "\' ORDER BY orderTimestamp DESC LIMIT 5";
         }

         List<List<String>> orders = esql.executeQueryAndReturnResult(query);
         String[] headers = {"OrderID:", "placed by:", "storeID:", "totalPrice:", "orderTimestamp:", "orderStatus:"};
         printformatted(headers, orders);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   
   
   
   public static void viewOrderInfo(PizzaStore esql, String login) {
      String id = "";
      try{
         System.out.println("Enter Order ID: ");
         id = in.readLine();
         String role = returnrole(esql, login);
         System.out.println(role);
         if(role.trim().equals("Customer")){
            
            String ordercustomer = esql.executeQueryAndReturnResult("SELECT u.login from Users u, Foodorder o WHERE o.orderID = \'" + id + "\' and u.login = o.login;").get(0).get(0);
            if(!ordercustomer.equals(login)){
               System.out.println("error, please only look up your own order!");
            } else{
               System.out.println(esql.executeQueryAndReturnResult("SELECT * FROM FoodOrder WHERE orderID = \'" + id + "\';"));
            }
         } else{
            System.out.println(esql.executeQueryAndReturnResult("SELECT * FROM FoodOrder WHERE orderID = \'" + id + "\';"));
         }


      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void viewStores(PizzaStore esql) {
      List<List<String>> stores = null;
      try{
         stores = esql.executeQueryAndReturnResult("SELECT * FROM Store ORDER BY storeID;");
         String[] headers = { "StoreID:", "Address:", "City:", "State:", "Open?:", "Review Score:" };

         printformatted(headers, stores);
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void updateOrderStatus(PizzaStore esql, String login) {
      //check if user is manager/driver
      String orderID = "";
      String newOrderStatus = "";
      try{
         String role = returnrole(esql, login);
         System.out.println(role);
         if (role.trim().equals("manager") || role.trim().equals("driver")) {
            System.out.println("Enter Order ID: ");
            orderID = in.readLine();
            System.out.println("Enter new Order Status:");
            newOrderStatus = in.readLine();
            esql.executeUpdate("UPDATE FoodOrder SET orderStatus = \'" + newOrderStatus.trim() + "\' WHERE orderID = " + orderID + ";");
         } else{
            System.out.println("Error, must be manager or driver!");
         }
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void updateMenu(PizzaStore esql, String login) {
      String role = returnrole(esql, login);
      if(role.trim().equals("manager")){
         System.out.println("1. Create new item");
         System.out.println("2. Remove an item");
         System.out.println("3. Modify existing item");
         switch(readChoice()){
            case 1: createItem(esql); break;
            case 2: removeItem(esql); break;
            case 3: modifyItem(esql); break;
         }
      } else{
         System.out.println("error, must be manager to update menu!");
      }
   }
   public static void updateUser(PizzaStore esql, String login) {
      String oldlogin = "";
      String editing = "";
      String newvalue = "";
      try{
         String role = returnrole(esql, login);
         if(role.trim().equals("manager")){
            System.out.println("Enter login to edit user:");
            oldlogin = in.readLine();
            System.out.println("Edit role or login?: ");
            editing = in.readLine();
            System.out.println("enter new login/role: ");
            newvalue = in.readLine();

            if(editing.equals("login")){
               esql.executeUpdate("UPDATE Users SET \'" + editing + "\' = \'" + newvalue + "\' WHERE login = \'" + oldlogin + "\';");
            } else if(editing.equals("role")){
               esql.executeUpdate("UPDATE Users SET " + editing + " = \'" + newvalue + "\' WHERE login = \'" + oldlogin + "\';");
            }
         } else{
            System.out.println("must be manager to update role/login");
         }
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }

   public static void createItem(PizzaStore esql){
      String itemName = "";
      String ingredientlist = "";
      String itemType = "";
      String price = "";
      String description = "";

      try{
         System.out.println("Enter item name: ");
         itemName = in.readLine().replace("'", "''");
         System.out.println("Enter list of ingredients, seperated by comma: ");
         ingredientlist = in.readLine().replace("'", "''");
         System.out.println("Enter item type: ");
         itemType = in.readLine();
         System.out.println("Enter item price: ");
         price = in.readLine();
         System.out.println("Enter item description: ");
         description = in.readLine().replace("'", "''");

         esql.executeUpdate("INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES(\'" + itemName + "\', \' " + ingredientlist + "\', " + "\' " + itemType + "\', " + price + ", \' " + description + "\');");
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void removeItem(PizzaStore esql){
      String itemName = "";
      try{
         System.out.println("Enter item name: ");
         itemName = in.readLine().replace("'", "''");

         esql.executeUpdate("DELETE FROM Items where itemName = \'" + itemName + "\';");
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }
   public static void modifyItem(PizzaStore esql){
      String itemName = "";
      String field = "";
      String newOrderStatus = "";

      try{
         System.out.println("Enter item name: ");
         itemName = in.readLine().replace("'", "''");
         System.out.println("Enter field to change(Name, ingredients, type, price): ");
         field = in.readLine();
         System.out.println("Enter new value: ");
         newOrderStatus = in.readLine().replace("'", "''");
         
         if(field.toLowerCase().equals("name")){
            esql.executeUpdate("UPDATE Items SET itemName" + " = \'" + newOrderStatus + "\' WHERE itemName = \'" + itemName + "\';");
         } else if(field.toLowerCase().equals("type")){
            esql.executeUpdate("UPDATE Items SET typeOfItem" + " = \'" + newOrderStatus + "\' WHERE itemName = \'" + itemName + "\';");
         } else{
            esql.executeUpdate("UPDATE Items SET " + field.toLowerCase() + " = \'" + newOrderStatus + "\' WHERE itemName = \'" + itemName + "\';");
         }
      } catch(Exception e){
         System.out.println(e.getMessage());
      }
   }

   public static String returnrole(PizzaStore esql, String login){
      try{   
         String role = esql.executeQueryAndReturnResult("SELECT role FROM Users WHERE login = \'" + login + "\';").get(0).get(0);
         return role;
      } catch(Exception e){
         System.out.println(e.getMessage());
         return "";
      } 
   }
   
   public static void printformatted(String[] headers, List<List<String>> items){
      int[] colWidths = new int[headers.length];
      for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length(); // Start with the header length
         }

         for (List<String> row : items) {
            for (int i = 0; i < row.size(); i++) {
               colWidths[i] = Math.max(colWidths[i], row.get(i).length());
            }
         }

         for (int i = 0; i < headers.length; i++) {
            System.out.print(String.format("%-" + (colWidths[i] + 2) + "s", headers[i]));
         }
         System.out.println();

         for (List<String> row : items) {
            for (int i = 0; i < row.size(); i++) {
               System.out.print(String.format("%-" + (colWidths[i] + 2) + "s", row.get(i)));
            }
            System.out.println();
         }
   } 


}//end PizzaStore

