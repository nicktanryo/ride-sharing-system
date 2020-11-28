import java.util.Scanner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.sql.*;

public class Passenger extends Object{

    static Scanner integerScanner = new Scanner(System.in);
    static Scanner stringScanner = new Scanner(System.in);

    static int MIN_PASSENGERS = 1;
    static int MAX_PASSENGERS = 8;
    static int MAX_DRIVING_YEARS = 80;

    /*******************
     ** MAIN FUNCTION **
     *******************/
    public static void run() {

        boolean passsengerApplicationIsRunning = true;

        while(passsengerApplicationIsRunning) {
            printPassengerMenu();

            boolean inputIsValid = false;

            while(!inputIsValid) {
                try {
                    int passengerAction = integerScanner.nextInt();
    
                    switch(passengerAction) {
                        case 1:
                            requestRide();
                            inputIsValid = true;
                            break;
                        case 2:
                            checkTripRecords();
                            inputIsValid = true;
                            break;
                        case 3:
                            passsengerApplicationIsRunning = false;
                            inputIsValid = true;
                            break;
                        default:
                            System.err.println(Error.INVALID_INPUT);
                    }
                } catch (Exception err) {
                    System.err.println(Error.INVALID_INPUT);
                    integerScanner.next();
                }
            }
        }
    }

    // print menu for passenger - done
    private static void printPassengerMenu() {
        System.out.println("Passenger, what would you like to do?");
        System.out.println("1. Request a ride");
        System.out.println("2. Check trip records");
        System.out.println("3. Go back");
        System.out.println("Please enter [1-3].");
    }

    /**
     * request ride for passenger - done
     */
    private static void requestRide() {

        boolean driverFound = false;

        while(!driverFound) {

            int passengerId = getPassengerId();

            try {
                // check whether passenger has made a request before
                String checkPassengerRequest = String.format("select count(*) from request where passenger_id=%d and taken=false", passengerId);
                ResultSet numberOfRequest = DatabaseConnection.executeQuery(checkPassengerRequest);
                numberOfRequest.next();
                if(numberOfRequest.getInt(1) > 0) {
                    System.out.println(Error.PASSENGER_HAS_OPEN_REQUEST);
                    break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            int numberOfPassengers = getNumberOfPassengers();
            String startLocation = getStartLocation().toLowerCase();
            String destination = getDestination().toLowerCase();
            while(destination.compareTo(startLocation) == 0) {
                System.out.println(Error.SAME_START_LOCATION_AND_DESTIONATION);
                destination = getDestination().toLowerCase();
            }
            String carModel = getCarModel().toLowerCase();
            int drivingYears = getDrivingYears();
            
            // System.out.println("ID                   : " + passengerId );
            // System.out.println("Number of Passengers : " + numberOfPassengers);
            // System.out.println("Start Location       : " + startLocation);
            // System.out.println("Destination          : " + destination);
            // System.out.println("Car Model            : " + carModel);
            // System.out.println("Driving Years        : " + drivingYears);
     
            try {
                Connection connection = DatabaseConnection.connect();

                // create a request 
                String mysqlStatement = "insert into request(passenger_id, start_location, destination, model, passengers, driving_years) values(?, ?, ?, ?, ?, ?)";

                // prepare statement for request
                PreparedStatement statement = connection.prepareStatement(mysqlStatement);

                statement.setInt(1, passengerId);
                statement.setString(2, startLocation);
                statement.setString(3, destination);
                statement.setString(4, carModel);
                statement.setInt(5, numberOfPassengers);
                statement.setInt(6, drivingYears);

                // find any drivers that fulfill criteria
                String findDriverStatement =    String.format(
                                            "select COUNT(*) " + 
                                            "from driver d, vehicle v " +
                                            "where d.vehicle_id=v.id and d.driving_years >= %d and lower(v.model) regexp \".*%s.*\" and v.seats >=%d", 
                                            drivingYears, carModel, numberOfPassengers);

                ResultSet driver = DatabaseConnection.executeQuery(findDriverStatement);

                driver.next();
                int driverAvailable = driver.getInt(1);

                if(driverAvailable == 0) {
                    System.out.println(Error.DRIVER_NOT_FOUND);
                } else {
                    int rowAffected = statement.executeUpdate();
                    if(rowAffected > 0) {
                        System.out.println("Your request is placed. " + driverAvailable + " driver" + ((driverAvailable > 1) ? "s" : "") + " are able to take the request.");
                        driverFound = true;
                    }
                }
            } catch(Exception err) {
                System.out.println(Error.QUERY_FAILURE);
                System.out.println(err.getMessage());
                driverFound = true;
            }

        }

    }
 
    /**
     * check passenger's trips record - done
     */
    private static void checkTripRecords() {

        int id = getPassengerId();
        String startDate = getStartDate();
        String endDate = getEndDate(startDate);
        String destination = getDestination().toLowerCase();

        // System.out.println("ID                   : " + id );
        // System.out.println("Start Date           : " + startDate);
        // System.out.println("End Date             : " + endDate);
        // System.out.println("Destination          : " + destination);

        try {
            String mysqlStatement = String.format(
                "select t.id, d.name, v.id, v.model, t.start_time, t.end_time, t.fee, t.start_location, t.destination " + 
                "from trip t, driver d, vehicle v " +
                "where t.passenger_id=%d and t.driver_id=d.id and d.vehicle_id=v.id and date(t.start_time)>=\"%s\" and date(t.end_time)<=\"%s\" and lower(t.destination)=\"%s\"",
                id, startDate, endDate, destination
            );

            ResultSet trips = DatabaseConnection.executeQuery(mysqlStatement);

            if(!trips.isBeforeFirst()) {
                System.out.println(Error.TRIPS_NOT_FOUND);
            } else {
                System.out.println("Trip_id, Driver Name, Vehicle ID, Vehicle Model, Start, End, Fee, Start Location, Destination");
                while(trips.next()) {
                    System.out.print(trips.getInt(1) + ", ");
                    System.out.print(trips.getString(2) + ", ");
                    System.out.print(trips.getString(3) + ", ");
                    System.out.print(trips.getString(4) + ", ");
                    System.out.print(trips.getString(5) + ", ");
                    System.out.print(trips.getString(6) + ", ");
                    System.out.print(trips.getInt(7) + ", ");
                    System.out.print(trips.getString(8) + ", ");
                    System.out.print(trips.getString(9) + "\n");
                }
            }
        } catch(Exception err) {
            System.err.println(err);
        }
    }

    // get passenger id - done
    private static int getPassengerId() {

        boolean inputIsValid = false;
        int id = 0;

        while(!inputIsValid) {
            try {
                System.out.println("Please enter your ID.");
                id = integerScanner.nextInt();

                // check whether ID exist in database
                String mysqlStatement = String.format(
                    "select id " + 
                    "from passenger " + 
                    "where id = %d ", id);

                ResultSet idInDatabase = DatabaseConnection.executeQuery(mysqlStatement);

                if(!idInDatabase.isBeforeFirst()) {
                    System.out.println(Error.PASSENGER_ID_NOT_FOUND);
                } else {
                    inputIsValid = true;
                }
            } catch (InputMismatchException err) {
                System.err.println(Error.INVALID_INPUT);
                integerScanner.next();
            } catch(SQLException err) {
                System.out.println(err.getMessage());
            }
        }
        return id;
    }

    // get passengers number - done
    private static int getNumberOfPassengers() {

        boolean inputIsValid = false;
        int numberOfPassengers = 0;

        while(!inputIsValid) {
            System.out.println("Please enter the number of passengers.");
            try {
                numberOfPassengers = integerScanner.nextInt();

                if (numberOfPassengers > MAX_PASSENGERS || numberOfPassengers < MIN_PASSENGERS) {
                    System.err.println(Error.INVALID_NUMBER_OF_PASSENGER);
                } else {
                    inputIsValid = true;
                }
            } catch (Exception err) {
                System.err.println(Error.INVALID_INPUT);
                integerScanner.next();
            }
        }

        return numberOfPassengers;
    }

    // get start location - done
    private static String getStartLocation() {

        boolean inputIsValid = false;
        String startLocation = "";

        while(!inputIsValid) {
            System.out.println("Please enter the start location.");
            startLocation = stringScanner.nextLine().toLowerCase();

            String mysqlStatement = String.format(
                    "select name " + 
                    "from taxi_stop " + 
                    "where name = \"%s\" ", startLocation);

            try {
                ResultSet locationInDatabase = DatabaseConnection.executeQuery(mysqlStatement);
    
                if (!locationInDatabase.isBeforeFirst()){
                    System.err.println(Error.START_LOCATION_NOT_FOUND);
                } else {
                    inputIsValid = true;
                }
            } catch (SQLException err) {
                System.err.println(err.getMessage());
            }
        }

        return startLocation;
    }

    // get destination - done
    private static String getDestination() {

        boolean inputIsValid = false;
        String destination = "";

        while(!inputIsValid) {
            System.out.println("Please enter the destination.");
            destination = stringScanner.nextLine().toLowerCase();

            String mysqlStatement = String.format(
                    "select name " + 
                    "from taxi_stop " + 
                    "where name = \"%s\" ", destination);
            
            try {
                ResultSet locationInDatabase = DatabaseConnection.executeQuery(mysqlStatement);

                if (!locationInDatabase.isBeforeFirst()){
                    System.out.println(Error.DESTINATION_NOT_FOUND);
                } else {
                    inputIsValid = true;
                }
            } catch (SQLException err) {
                System.err.println(err.getMessage());
            }
        }

        return destination;
    }

    // get car model - done
    private static String getCarModel(){

        boolean inputIsValid = false;
        String model = "";

        while(!inputIsValid) {
            System.out.println("Please enter the model. (Please enter to skip)");
            model = stringScanner.nextLine().toLowerCase();

            if(model.compareTo("") == 0) {
                stringScanner = new Scanner(System.in);
                return "";
            } else {
                // check car model in database
                String mysqlStatement = String.format(
                    "select count(*) " + 
                    "from vehicle " + 
                    "where lower(model) regexp \"%s\" ", model);

                try {
                    ResultSet modelInDatabase = DatabaseConnection.executeQuery(mysqlStatement);
                    modelInDatabase.next();

                    if(modelInDatabase.getInt(1) == 0) {
                        System.err.println(Error.CAR_MODEL_NOT_FOUND);
                    } else {
                        inputIsValid = true;
                    }

                } catch (SQLException err) {
                    System.err.println(err.getMessage());
                }
            }
        }
        return model;
    }

    // get driver's driving years - done
    private static int getDrivingYears() {

        boolean inputIsValid = false;
        int drivingYears = 0;

        while(!inputIsValid) {
            System.out.println("Please enter the minimum driving years of the driver. (Please enter to skip)");
            String passengerInput = stringScanner.nextLine();

            // if passenger does not have any preference
            if (passengerInput.compareTo("") == 0) {
                return 0;
            } else {
                try {
                    int tempDrivingYears = Integer.parseInt(passengerInput);
    
                    if (tempDrivingYears > MAX_DRIVING_YEARS || tempDrivingYears < 0) {
                        System.err.println(Error.INVALID_INPUT);
                    } else {
                        drivingYears = tempDrivingYears;
                        inputIsValid = true;
                    }
                } catch (NumberFormatException err) {
                    System.err.println(Error.INVALID_INPUT);
                    stringScanner.next();
                }
            }
        }
        return drivingYears;
    }

    // date validation - done
    private static boolean DateIsValid(String date){

        try {
            SimpleDateFormat customDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            customDateFormat.setLenient(false);
            customDateFormat.parse(date);
            return true;
        } catch (IllegalArgumentException err) {
            System.out.println(err.getMessage());
            return false;
        } catch (ParseException err) {
            return false;
        }

    }

    // get start date - done
    private static String getStartDate() {

        boolean inputIsValid = false;
        String startDate = "";

        while(!inputIsValid) {

            System.out.println("Please enter the start date.");
            startDate = stringScanner.nextLine();

            if (DateIsValid(startDate)) {
                inputIsValid = true;
            } else {
                System.err.println(Error.INVALID_DATE_FORMAT);
            }

        }

        return startDate;
    }

    // get end date - done
    private static String getEndDate(String startDate) {

        boolean inputIsValid = false;
        String endDate = "";

        while(!inputIsValid) {

            System.out.println("Please enter the end date.");
            endDate = stringScanner.nextLine();

            if (DateIsValid(endDate)) {
                SimpleDateFormat customDate = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    Date start_date = customDate.parse(startDate);
                    Date end_date = customDate.parse(endDate);

                    if(start_date.compareTo(end_date) > 0) {
                        System.err.println(Error.INVALID_START_AND_END_DATE);
                    } else {
                        inputIsValid = true;
                    }

                } catch (Exception e) {
                    System.err.println(Error.INVALID_DATE_FORMAT);
                }

            } else {
                System.err.println(Error.INVALID_DATE_FORMAT);
            }

        }

        return endDate;

    }
}
