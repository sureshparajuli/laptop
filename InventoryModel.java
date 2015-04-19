/** @author Clara MCTC Java Programming Class */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


public class InventoryModel {

    // JDBC driver name, protocol, used to create a connection to the DB
    private static String protocol = "jdbc:derby:";
    private static String dbName = "laptopInventoryDB";



    //  Database credentials - for embedded, usually defaults. A client-server DB would need to authenticate connections
    private static final String USER = "temp";
    private static final String PASS = "password";


    InventoryController myController;

    Statement statement = null;

    Connection conn = null;

    ResultSet rs = null;

    LinkedList<Statement> allStatements = new LinkedList<Statement>();

    PreparedStatement psAddLaptop = null;


    public InventoryModel(InventoryController controller) {

        this.myController = controller;

    }


    public boolean setupDatabase() {

        return setupDatabase(false);

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


        //Remove the test data for real program
        try {
            addTestData();
        }
        catch (Exception sqle) {

            System.err.println("Unable to add test data to database. Error message and stack trace follow");
            System.err.println(sqle.getMessage());
            sqle.printStackTrace();
            return false;


        }

        //At this point, it seems like everything worked.

        return true;
    }



    private void createTable(boolean deleteAndRecreate) throws SQLException {


        String createLaptopTableSQL = "CREATE TABLE laptops (id int PRIMARY KEY GENERATED ALWAYS AS IDENTITY, make varchar(30), model varchar(30), staff varchar(50))";
        String deleteTableSQL = "DROP TABLE laptops";

        try {
            statement.executeUpdate(createLaptopTableSQL);
            System.out.println("Created laptop table");

        } catch (SQLException sqle) {
            //Seems the table already exists, or some other error has occurred.
            //Let's try to check if the DB exists already by checking the error code returned. If so, delete it and re-create it


            if (sqle.getSQLState().startsWith("X0") ) {    //Error code for table already existing starts with XO
                if (deleteAndRecreate == true) {

                    System.out.println("laptops table appears to exist already, delete and recreate");
                    try {
                        statement.executeUpdate(deleteTableSQL);
                        statement.executeUpdate(createLaptopTableSQL);
                    } catch (SQLException e) {
                        //Still doesn't work. Throw the exception.
                        throw e;
                    }
                } else {
                    //do nothing - if the table exists, leave it be.
                }

            } else {
                //Something else went wrong. If we can't create the table, no point attempting
                //to run the rest of the code. Throw the exception again to be handled elsewhere in the program.
               throw sqle;
            }
        }
    }

    private void createConnection() throws SQLException {

            conn = DriverManager.getConnection(protocol + dbName + ";create=true", USER, PASS);
            statement = conn.createStatement();
            allStatements.add(statement);

    }


    private void addTestData() throws LaptopDataAccessException {
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
                throw new LaptopDataAccessException(error, sqle);
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



    public void addLaptop(Laptop laptop) throws LaptopDataAccessException {


        //Create SQL query to add this laptop info to DB

        String addLaptopSQLps = "INSERT INTO laptops (make, model, staff) VALUES ( ? , ? , ?)" ;

        try {
            psAddLaptop = conn.prepareStatement(addLaptopSQLps, psAddLaptop.RETURN_GENERATED_KEYS);
            allStatements.add(psAddLaptop);
            psAddLaptop.setString(1, laptop.getMake());
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
            throw new LaptopDataAccessException(errorMessage, sqle);
        }
    }


    /** @return list of laptops in the DB (will be empty list if no laptops found in DB)
     *  @throws LaptopDataAccessException if SQL error occurs
     *
     */
    public LinkedList<Laptop> displayAllLaptops() throws LaptopDataAccessException {

        LinkedList<Laptop> allLaptops = new LinkedList<Laptop>();

        String displayAll = "SELECT * FROM laptops";
        try {
            rs = statement.executeQuery(displayAll);
        }
        catch (SQLException sqle) {
            String errorMessage = "Database error fetching all laptops";
            throw new LaptopDataAccessException(errorMessage, sqle);
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
            throw new LaptopDataAccessException(errorMessage, sqle);
        }

        //if we get here, everything should have worked...
        //Return the list of laptops, which will be empty if there is no data in the database
        return allLaptops;
    }


    /** @return laptop object for a laptop ID.  Returns null if the ID is not found.
     *  @throws LaptopDataAccessException if SQL error occurs
     *
     */

    public Laptop fetchLaptop(int id) throws LaptopDataAccessException{
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
                    throw new LaptopDataAccessException("More than one laptop in database for ID " + id);
                }
            } else {
                //rs has no rows - no laptop found - return null
                return null;
            }

        } catch (SQLException sqle) {
            String errorMessage = "Database error fetching laptop for ID " + id + " check inner exception for details";
            throw new LaptopDataAccessException(errorMessage, sqle);

        }

    }

    //TODO test this method. Use it in the code.

    /** @return true if laptop update is successful (1 row is changed) or false if laptop not updated = this will be because the id isn't in the database
     * @throws LaptopDataAccessException if more than one laptop with that ID found or in the case of general DB errors */

    public boolean reassignLaptop(int id, String newUser) throws LaptopDataAccessException{

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
                throw new LaptopDataAccessException("More than one laptop with laptop id " + id);
            }
        } catch (SQLException sqle) {
            //Wrap the SQLException in our own custom exception and re-throw it for Controller to handle
            String errorMessage = "Error changing staff assignment laptop number " + id;
            throw new LaptopDataAccessException(errorMessage, sqle);


        }

    }
    }




