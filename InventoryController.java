/** @author Clara MCTC Java Programming Class */


import java.util.LinkedList;

public class InventoryController {

    //This class converses with the database and the UI. It checks DB requests for errors and forwards messages to UI.
    //Some DB errors are recoverable, some are not. The ones that are not, the program will quit (e.g. database errors).
    //The ones that can be fixed (e.g. user not found in DB) will be sent to InventoryView so it can ask the user to try again

    //Custom Exception class used - only so we can use a custom exception name. LaptopDataAccessExceptions often
    //wrap the source exception (e.g. SQLException) so that can be used, logged, whatever. Here we'll re-throw exceptions
    //and crash the program so the programmer knows about the error and can fix it. There's nothing the user can do if
    //the programmer has written faulty SQL so it's better to have the controller throw the errors so they can be detected
    //and fixed during the development process.

    static InventoryModel db ;


    public static void main(String[] args) {

        //Add a shutdown hook.
        //http://hellotojavaworld.blogspot.com/2010/11/runtimeaddshutdownhook.html
        AddShutdownHook closeDBConnection = new AddShutdownHook();
        closeDBConnection.attachShutdownHook();
        //Can put code in here to try to shut down the DB connection in a tidy manner if possible.
        //This code runs if you click the Stop button in IntelliJ console.

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


    public void requestAddLaptop(Laptop l) {

        //This message should arrive from the UI. Send a message to the db to request that this laptop is added.
        //Throws LaptopDataAccessException in case of unrecoverable errors e.g. database corrupted or programmer
        //error in writing database code or SQL statements.

        try {
            db.addLaptop(l);
            System.out.println("Laptop " + l + " added");

        }  catch (LaptopDataAccessException le) {
            System.out.println("Failed to add laptop " + le);
            throw le;   //crash the program, programmer needs to fix this
        }

    }

    public LinkedList<Laptop> requestAllInventory() {


        //This message should arrive from the UI. Send a message to the db to request all laptop data.
        //Returns a list of laptop objects
        //Throws LaptopDataAccessException in case of unrecoverable errors e.g. database corrupted or programmer
        //error in writing database code or SQL statements.

        try {
            LinkedList<Laptop> allLaptops = db.displayAllLaptops();
            return allLaptops;
        } catch (LaptopDataAccessException le) {
            System.out.println("Controller detected error in fetching laptops from database");
            throw le;   //Crash program, programmer needs to fix
        }

    }

    public Laptop requestLaptopById(int id) {

        //This message should arrive from the UI. Send a message to the db to request this laptop.
        //Returns a Laptop object if laptop is found, or null if it is not found.

        //Throws LaptopDataAccessException in case of unrecoverable errors e.g. database corrupted or programmer
        //error in writing database code or SQL statements;

        // Also crashes in the event of more than one laptop found for a laptop ID  //TODO is this the right approach for this case?

        try {
            Laptop l = db.fetchLaptop(id);
            return l;   //This will be null if laptop is not found
        }
        catch (LaptopDataAccessException le) {
            System.out.println("Error fetching laptop (request laptop by ID)");
            throw le;  //Crash program, programmer must fix
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
                // in the root directory of your project.   //TODO remove (?) MySQL handles crashed programs better(?)
                InventoryController.db.cleanup();
            }
        });
    }
}
