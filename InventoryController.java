/** @author Clara MCTC Java Programming Class */

import java.sql.SQLException;
import java.util.LinkedList;

public class InventoryController {

    //This class converses with the database and the UI. It checks DB requests for errors and forwards messages to UI.
    //Some DB errors are recoverable, some are not. The ones that are not, the program will quit. The ones that can be
    //fixed

    //Custom Exception class used - only so we can use a custom exception name. LaptopDataAccessExceptions often
    //wrap the source exception (e.g. SQLException) so that can be used, logged, whatever.

    static InventoryModel db ;


    public static void main(String[] args) {

        //Add a shutdown hook.
        //http://hellotojavaworld.blogspot.com/2010/11/runtimeaddshutdownhook.html
        AddShutdownHook closeDBConnection = new AddShutdownHook();
        closeDBConnection.attachShutdownHook();
        //Can put code in here to try to shut down the DB connection in a tidy manner if possible. This code runs if you click the Stop button.

        try {
            InventoryController controller = new InventoryController();

            db = new InventoryModel(controller);

            boolean setup = db.setupDatabase();

            if (setup == false) {
                System.out.println("Error setting up database, see error messages. Clean up database connections.... Quitting program ");

                db.cleanup();

                System.out.println("Quitting program ");

                System.exit(-1);   //Non-zero exit codes indicate errors.
            }

            new InventoryView(controller).launchUI();
        }

        finally {

            if (db != null) {
                db.cleanup();
            }
        }

    }

    public boolean requestAddLaptop(Laptop l) {

        //This message should arrive from the UI. Send a message to the db to request that this laptop is added.
        //Return error message, if any. Return true if transaction was successful.
        try {
            db.addLaptop(l);
            System.out.println("Laptop " + l + " added");
            return true;
        }  catch (LaptopDataAccessException le) {
            System.out.println("Failed to add laptop " + le);
            return false;
        }

    }

    public LinkedList<Laptop> requestAllInventory() {

        try {
            LinkedList<Laptop> allLaptops = db.displayAllLaptops();
            return allLaptops;
        } catch (LaptopDataAccessException le) {
            System.out.println("Controller detected error in fetching laptops from database");
            return null;   //Null means error. View can deal with how to display error to user.
        }

    }

    public Laptop requestLaptopById(int id) {

        try {
            Laptop l = db.fetchLaptop(id);
            return l;   //This will be null 
        }
        catch (LaptopDataAccessException sqle) {
            System.out.println("Error fetching laptop (request laptop by ID)");
            return null;
        }


    }
}

class AddShutdownHook {
    public void attachShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown hook: program closed, attempting to shut database connection");
                //Unfortunately this doesn't seem to be called when a program is restarted in IntelliJ.
                //Avoid restarting your programs, always shut down properly.
                // If you do restart your program, and you get an existing connection error you can
                //delete your database folder. In this project it's a folder called laptopinventoryDB (or similar)
                // in the root directory of your project.
                InventoryController.db.cleanup();
            }
        });
    }
}
