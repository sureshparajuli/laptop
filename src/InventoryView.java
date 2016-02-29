/** @author Clara MCTC Java Programming Class */

import java.util.LinkedList;
import java.util.Scanner;

public class InventoryView {

    private final int QUIT = 5;   //Modify if you add more menu items.
    //TODO Can you think of a more robust way of handling menu options which would be easy to modify with a varying number of menu choices?

    //TODO number validator class

    //TODO way to back out of choice



    InventoryController myController;
    Scanner s;

    InventoryView(InventoryController c) {
        myController = c;
        s = new Scanner(System.in);
    }


    public void launchUI() {
        //This is a text-based UI. Probably a GUI in a real program

        while (true) {

            int userChoice = displayMenuGetUserChoice();

            if (userChoice == QUIT ) {
                break;
            }

            doTask(userChoice);
        }

    }

    private void doTask(int userChoice) {

        switch (userChoice) {

            case 1:  {
                displayAllInventory();
                break;
            }
            case 2: {
                addNewLaptop();
                break;
            }
            case 3 : {
                //TODO finish this
                //System.out.println("Reassign laptop - In the process of being implemented");
                reassignLaptop();
                break;
            }
            case 4 : {
                //TODO implement this
                //System.out.println("Retire laptop - Not yet implemented");
                deleteExistingLaptop();
                break;
            }
        }
    }

    //method to reassign laptop
    private void reassignLaptop() {
        //TODO finish this
        //Ask for laptop ID
        //Fetch laptop info and display for user to confirm this is the correct laptop
        int id = 0;

        boolean moreReassign = true;

        while (moreReassign) {
            //declare invalid input chance count
            int invalidInputChance = 0;
            boolean done = false;
            System.out.println("Enter laptop ID to reassign");
            //loop until laptop id is numeric or entered 3 times
            while (!done) {
                try {
                    id = Integer.parseInt(s.nextLine());
                    done = true;
                } catch (NumberFormatException nf) {
                    invalidInputChance++;
                    if (invalidInputChance == 3) {
                        System.out.println("ERROR: invalid laptop Id entered 3 times, exiting reassign laptop menu");
                        return;
                    } else {
                        System.out.println("ERROR: Invalid input, enter the laptop Id (number)");
                    }
                }
            }

            Laptop laptop = myController.requestLaptopById(id);
            //TODO once laptop has been found, ask for new staff member's name
            //TODO Write this to the database, see draft method in InventoryModel
            if (laptop != null) {
                System.out.println("Please confirm the following laptop to be reassigned");//confirm the laptop to reassign
                System.out.println(laptop);
                String confirm = s.nextLine();

                if (confirm.equalsIgnoreCase("N")) {
                    continue;
                }

                System.out.println("Enter the staff to reassign");//ask for staff name to reassign
                String staffName = s.nextLine();

                //Remember InventoryModel.reassignLaptop returns true for success, false if laptop with this ID is not found
                boolean reassignSuccess = myController.requestReassignLaptop(id, staffName);
                if (reassignSuccess) {
                    System.out.println("Laptop Id = " + id + " reassigned to new user = " + staffName);
                } else {
                    System.out.println("Laptop Id = " + id + " could not be reassigned to new user = " + staffName);
                }
            } else {
                System.out.println("Laptop with ID = " + id + " not found.");
            }
            moreReassign = false;
        }
    }

    //delete the laptop by id
    private void deleteExistingLaptop() {
        //get the id for the new laptop
        int laptopId = 0;
        boolean done = false;
        int invalidInputChance = 0;

        System.out.println("Please enter the laptop Id to delete : ");
        //loop until laptop id is numeric or entered 3 times
        while (!done) {
            try {
                laptopId = Integer.parseInt(s.nextLine());
                done = true;
            }  catch (NumberFormatException nf) {
                invalidInputChance++;
                if (invalidInputChance == 3) {
                    System.out.println("ERROR: invalid laptop Id entered 3 times, exiting retire laptop menu");
                    //done = true;
                    return;
                } else {
                    System.out.println("ERROR: Invalid input, enter the laptop Id (number)");
                }
            }
        }
        myController.requestDeleteLaptopById(laptopId);
    }

    private void addNewLaptop() {

        //Get data about new laptop from user

        System.out.println("Please enter make of laptop (e.g. Toshiba, Sony) : ");
        String make = s.nextLine();

        System.out.println("Please enter model of laptop (e.g. Macbook, X-123) : ");
        String model = s.nextLine();

        System.out.println("Please enter name of staff member laptop is assigned to : ");
        String staff = s.nextLine();

        Laptop l = new Laptop(make, model, staff);

        myController.requestAddLaptop(l);

        System.out.println("New laptop added to database");


    }

    private void displayAllInventory() {

        LinkedList<Laptop> allLaptops = myController.requestAllInventory();
        if (allLaptops.isEmpty()) {
            System.out.println("No laptops found in database");
        } else {
            System.out.println("List of all laptops in the database:");
            for (Laptop l : allLaptops) {
                System.out.println(l);   //Call the toString method in Laptop
            }
        }
    }


    private void displayLaptopById(int id) {
        Laptop l = myController.requestLaptopById(id);
        if (l == null) {
            System.out.println("Laptop " + id + " not found");
        } else {
            System.out.println(l);   //Call the toString method in Laptop

        }
    }

    private int displayMenuGetUserChoice() {

        boolean inputOK = false;
        int userChoice = -1;

        while (!inputOK) {

            System.out.println("1. View all inventory");
            System.out.println("2. Add a new laptop");
            System.out.println("3. Reassign a laptop to another staff member");
            System.out.println("4. Retire a laptop");
            System.out.println(QUIT + ". Quit program");

            System.out.println();
            System.out.println("Please enter your selection");

            String userChoiceStr = s.nextLine();
            try {
                userChoice = Integer.parseInt(userChoiceStr);
                if (userChoice < 1  ||  userChoice > 5) {
                    System.out.println("Please enter a number between 1 and 5");
                    continue;
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Please enter a number");
                continue;
            }
            inputOK = true;

        }

        return userChoice;

    }
}