import java.util.*;
import java.sql.*;
import java.io.*;
import java.nio.file.Paths;


public class Manager extends Object {
    
    
    //static Scanner integerScanner = new Scanner(System.in);
    //static Scanner stringScanner = new Scanner(System.in); 

    public static void run(){
        boolean managerApplicationIsRunning = true;
                    
        while(managerApplicationIsRunning) {
            printManagerMenu();
                    
            boolean inputIsValid = false;
                    
            while(!inputIsValid) {
                try {
                    int managerAction = Main.scanner.nextInt();
        
                    switch(managerAction) {
                        case 1:
                            findTrips();
                            inputIsValid = true;
                            break;
                        case 2:
                            managerApplicationIsRunning = false;
                            inputIsValid = true;
                            break;
                        default:
                            System.err.println(Error.INVALID_INPUT);
                    }
                } catch (Exception err) {
                    System.err.println(Error.INVALID_INPUT);
                    Main.scanner.next();
                }
            }
        }
    }
     
    //print menu for manager
    private static void printManagerMenu() {
        System.out.println("Manager, what would you like to do?");
        System.out.println("1. Find trips");
        System.out.println("2. Go back");
        System.out.println("Please enter [1-2].");
    }
    
    // find trips
    private static void findTrips(){
        int minimumDistance = getTravelDistance("minimum");
        boolean maxDistanceIsValid = false;
        int maximumDistance = -1;
        while (!maxDistanceIsValid) {
            maximumDistance = getTravelDistance("maximum");
            if(maximumDistance < minimumDistance) {
                System.out.println(Error.MAX_DISTANCE_LOWER);
            } else {
                maxDistanceIsValid = true;
            }
        }

        try {

            String tripSQL = String.format(
                "select t.id, d.name, p.name, t.start_location, t.destination, t.fee " +
                "from trip t, driver d, passenger p, taxi_stop start_location, taxi_stop destination " +
                "where t.driver_id=d.id and t.passenger_id=p.id and t.start_location=start_location.name and t.destination=destination.name and t.fee is not null and " + 
                "(abs(start_location.location_x-destination.location_x)+abs(start_location.location_y-destination.location_y)) >= %d and " + 
                "(abs(start_location.location_x-destination.location_x)+abs(start_location.location_y-destination.location_y)) <= %d",
                minimumDistance, maximumDistance
            );

            ResultSet trips = DatabaseConnection.executeQuery(tripSQL);

            if(!trips.isBeforeFirst()) {
                System.out.println(Error.TRIPS_NOT_FOUND);
            } else{
                System.out.println("trip id, driver name, passenger name, start location, destination, duration");

                while(trips.next()) {
                    System.out.print(trips.getInt(1) + ", ");
                    System.out.print(trips.getString(2) + ", ");
                    System.out.print(trips.getString(3) + ", ");
                    System.out.print(trips.getString(4) + ", ");
                    System.out.print(trips.getString(5) + ", ");
                    System.out.print(trips.getInt(6) + "\n");
                }
            }

        } catch (SQLException err) {
            System.err.println(Error.QUERY_FAILURE);
            err.getMessage();
        }
    }

    // get travel distance
    private static int getTravelDistance(String status) {
        boolean inputIsValid = false;
        int distance = 0;

        while(!inputIsValid) {
            try {
                System.out.println("Please enter the " + status + " traveling distance.");
                distance = Main.scanner.nextInt();
                inputIsValid = true;
            } catch (InputMismatchException err) {
                System.err.println(Error.INVALID_INPUT);
                Main.scanner.next();
            }
        }
        return distance;
    }
}




