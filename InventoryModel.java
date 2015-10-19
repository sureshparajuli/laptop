/** @author Clara MCTC Java Programming Class */


//TODO remove custom exception, re-throw as RuntimeException

import org.omg.SendingContext.RunTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


public class InventoryModel {


    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";        //Configure the driver needed
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/";     //Connection string – where's the database?

    static final String DB_NAME = "laptop";

    static final String LAPTOP_TABLE_NAME = "user_laptops";

    static final String USER = "clara";   //TODO replace with your username
    static final String PASSWORD = "password";   //TODO replace with your password


    InventoryController myController;

    Statement statement = null;

    Connection conn = null;

    ResultSet rs = null;

    LinkedList<Statement> allStatements = new LinkedList();

    PreparedStatement psAddLaptop = null;

    public InventoryModel(InventoryController controller) {

        this.myController = controller;

        try {
            Class.forName(JDBC_DRIVER);

            //When the driver class is instantiated, it should register itself with the DriverManager.
            //You don't need to do anything else here.

        } catch (ClassNotFoundException ce) {
            //Log and re-throw.
            System.out.println("Can't find JDBC driver class");
            throw new RuntimeException(ce);     //TODO All other places too.
        }
    }


    public boolean setupDatabase() {

        //**TODO change this from true to false as you need for testing/debugging **//

        return setupDatabase(false);  //true = delete and recreate database, false = keep existing database

    }

    public boolean setupDatabase(boolean deleteAndRecreate) {

        try {

            createConnection();

        } catch (SQLException e) {

            System.err.println("Unable to connect to database. Error message and stack trace follow");
            System.err.println(e.getMessage());
            e.printStackTrace();
            return false;
        }


        try {

            createTable(deleteAndRecreate);

        } catch (SQLException sqle) {

            System.err.println("Unable to create database. Error message and stack trace follow");
            System.err.println(sqle.getMessage() + " " + sqle.getErrorCode());
            sqle.printStackTrace();
            return false;
        }


        //TODO Remove the test data for real program

        //If we are deleting and recreating the table, we'll need to add test data.
        if (deleteAndRecreate) {

            try {
                addTestData();

           } catch (Exception sqle) {

                System.err.println("Unable to add test data to database. Error message and stack trace follow");
                System.err.println(sqle.getMessage());
                sqle.printStackTrace();
                return false;

            }
        }
        //At this point, it seems like everything worked.

        return true;
    }


    private void createTable(boolean deleteAndRecreate) throws SQLException {


        String createLaptopTableSQL = "CREATE TABLE laptops (id int PRIMARY KEY AUTO_INCREMENT, make varchar(30), model varchar(30), staff varchar(50))";
        String deleteTableSQL = "DROP TABLE " + LAPTOP_TABLE_NAME;

        String doesTableExistSQL = "SHOW TABLES LIKE '" + LAPTOP_TABLE_NAME + "'";

        boolean laptopsTableAlreadyExists = false;


        try {
            rs = statement.executeQuery(doesTableExistSQL);
            if (rs.next()){
                if (rs.getString("Tables_in_laptop").equals(LAPTOP_TABLE_NAME)) {
                    //Table does exists
                    System.out.println(LAPTOP_TABLE_NAME + " does exist already");
                    laptopsTableAlreadyExists = true;
                } else {
                    System.out.println(LAPTOP_TABLE_NAME + " does not currently exist");
                    laptopsTableAlreadyExists = false;
                }
            }
        } catch (SQLException sqle) {
            System.out.println("Error testing if table " + LAPTOP_TABLE_NAME + " exists");
            throw sqle;
        }



        //Need to delete table and re-create
        if (deleteAndRecreate) {
            //Check if table exists - if it doesn't exist, deleting would cause error
            if (laptopsTableAlreadyExists) {
                statement.executeUpdate(deleteTableSQL);
                System.out.println("Deleted old version of table");
            }

            //Now table is deleted (if it existed). Create new table.
            statement.executeUpdate(createLaptopTableSQL);
            System.out.println("Created laptop table");

        }


        //Don't delete and re-create - just create if it doesn't exist
        else {
             if (laptopsTableAlreadyExists) {
                 //table does not exist yet
                 statement.executeUpdate(createLaptopTableSQL);
                 System.out.println("Created laptop table");
             }
        }
    }

    private void createConnection() throws SQLException {


            conn = DriverManager.getConnection(DB_CONNECTION_URL + DB_NAME, USER, PASSWORD);


            statement = conn.createStatement();
            allStatements.add(statement);

    }


    private void addTestData()  {
        // Add some test data.

        try {
                String addRecord1 = "INSERT INTO laptops (make, model, staff) VALUES ('Toshiba', 'XQ-45', 'Ryan' )";
                statement.executeUpdate(addRecord1);
                String addRecord2 = "INSERT INTO laptops (make, model, staff) VALUES ('Sony', '1234', 'Jane' )";
                statement.executeUpdate(addRecord2);
                String addRecord3 = "INSERT INTO laptops (make, model, staff) VALUES ('Apple', 'Air', 'Alex' )";
                statement.executeUpdate(addRecord3);

            } catch (SQLException sqle) {
                String error = "Unable to add test data, check validity of SQL statements?";
                throw new RuntimeException(error, sqle);
            }
        }



    public void cleanup() {
        try {
            if (rs != null) {
                rs.close();  //Close result set
                System.out.println("ResultSet closed");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

        //Close all of the statements. Stored a reference to each statement in allStatements so we can loop over all of them and close them all.
        for (Statement s : allStatements) {

            if (s != null) {
                try {
                    s.close();
                    System.out.println("Statement closed");
                } catch (SQLException se) {
                    System.out.println("Error closing statement");
                    se.printStackTrace();
                }
            }
        }

        try {
            if (conn != null) {
                conn.close();  //Close connection to database
                System.out.println("Database connection closed");
            }
        } catch (SQLException se) {
            System.out.println("Error closing database connection");
            se.printStackTrace();
        }
    }



    public void addLaptop(Laptop laptop) {


        //Create SQL query to add this laptop info to DB

        String addLaptopSQLps = "INSERT INTO laptops (make, model, staff) VALUES ( ? , ? , ?)" ;

        try {
            psAddLaptop = conn.prepareStatement(addLaptopSQLps, psAddLaptop.RETURN_GENERATED_KEYS);
            allStatements.add(psAddLaptop);
            psAddLaptop.setString(1, laptop.getMake());
            psAddLaptop.setString(2, laptop.getModel());
            psAddLaptop.setString(2, laptop.getModel());
            psAddLaptop.setString(3, laptop.getStaff());
            psAddLaptop.execute();

            //Retrieve new laptop ID and add it to the Laptop laptop so calling methods can use it.

            ResultSet keys = psAddLaptop.getGeneratedKeys();
            //We assume just one key, which will be the first thing in the ResultSet
            keys.next();
            int laptopID = keys.getInt(1);
            laptop.id = laptopID;


        }
        catch (SQLException sqle) {
            String errorMessage = "Error preparing statement or executing prepared statement to add laptop";
            throw new RuntimeException(errorMessage, sqle);
        }
    }


    /** @return list of laptops in the DB (will be empty list if no laptops found in DB)
     *  @throws java.lang.RuntimeException if SQL error occurs
     *
     */
    public LinkedList<Laptop> displayAllLaptops() {

        LinkedList<Laptop> allLaptops = new LinkedList();

        String displayAll = "SELECT * FROM laptops";
        try {
            rs = statement.executeQuery(displayAll);
        }
        catch (SQLException sqle) {
            String errorMessage = "Database error fetching all laptops";
            throw new RuntimeException(errorMessage, sqle);
        }


        try {
            while (rs.next()) {

                int id = rs.getInt("id");
                String make = rs.getString("make");
                String model = rs.getString("model");
                String staff = rs.getString("staff");
                Laptop l = new Laptop(id, make, model, staff);
                allLaptops.add(l);

            }
        } catch (SQLException sqle) {
            String errorMessage = "Error reading from result set after fetching all laptop data";
            throw new RuntimeException(errorMessage, sqle);
        }

        //if we get here, everything should have worked...
        //Return the list of laptops, which will be empty if there is no data in the database
        return allLaptops;
    }


    /** @return laptop object for a laptop ID.  Returns null if the ID is not found.
     *  @throws RuntimeException if SQL error occurs
     *
     */

    public Laptop fetchLaptop(int id) {
        try {
            String fetchLaptop = "SELECT * FROM laptops where id = ?";
            PreparedStatement psFetch = conn.prepareStatement(fetchLaptop);
            allStatements.add(psFetch);
            psFetch.setInt(1, id);
            rs = psFetch.executeQuery();

            //Expect only one row if laptop is in DB, or zero (0) rows if it is not.

            if (rs.next()) {
                String make = rs.getString("make");
                String model = rs.getString("model");
                String staff = rs.getString("staff");

                if (!rs.next()) {  //Make sure there are no more rows after the first row
                    Laptop l = new Laptop(id, make, model, staff);
                    return l;
                } else {
                    //more than one laptop found
                    //Error condition - more than one laptop for primary key ID is a problem that must be fixed
                    throw new RuntimeException("More than one laptop in database for ID " + id);
                }
            } else {
                //rs has no rows - no laptop found - return null
                return null;
            }

        } catch (SQLException sqle) {
            String errorMessage = "Database error fetching laptop for ID " + id + " check inner exception for details";
            throw new RuntimeException(errorMessage, sqle);

        }

    }

    //TODO test this method. Use it in the code.

    /** @return true if laptop update is successful (1 row is changed) or false if laptop not updated = this will be because the id isn't in the database
     * @throws java.lang.RuntimeException if more than one laptop with that ID found or in the case of general DB errors */

    public boolean reassignLaptop(int id, String newUser) {

        try {
            String reassignLaptop = "UPDATE laptops set staff = ? where id = ?";
            PreparedStatement psReassign = conn.prepareStatement(reassignLaptop);
            psReassign.setInt(1, id);
            psReassign.setString(2, newUser);
            //We expect exactly one row to be modified.
            int rowsModified = psReassign.executeUpdate();  //exceuteUpdate returns the number of rows modified so we can check to make sure exactly one row was changed - the row with the specific laptop
            if (rowsModified == 1) {
                return true;   //Success
            } else if (rowsModified == 0 ){
                //This means the laptop is not found. Return message that permits the user to try again - maybe a bad ID was entered?
                return false;
            } else {
                //rowsModified is not 0 or 1 - so more than 1 row was modified. (Can executeUpdate return negative numbers? I don't think so...)
                throw new RuntimeException("More than one laptop with laptop id " + id);
            }
        } catch (SQLException sqle) {
            //Wrap the SQLException in our own custom exception and re-throw it for Controller to handle
            String errorMessage = "Error changing staff assignment laptop number " + id;
            throw new RuntimeException(errorMessage, sqle);


        }

    }
    }




