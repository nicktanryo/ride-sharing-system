import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Driver extends Object{

    //static Scanner integerScanner = new Scanner(System.in);
    //static Scanner stringScanner = new Scanner(System.in);
    
    /*******************
     ** MAIN FUNCTION **
     *******************/
    public static void run() {

        boolean driverApplicationIsRunning = true;

        while(driverApplicationIsRunning) {
            printMenu();

            boolean inputIsValid = false;

            while(!inputIsValid) {
                try {
                    int driverAction = Main.scanner.nextInt();

                    switch(driverAction) {
                        case 1:
                            searchRequest();
                            inputIsValid = true;
                            break;
                        case 2:
                            takeRequest();
                            inputIsValid = true;
                            break;
                        case 3:
                            finishTrip();
                            inputIsValid = true;
                            break;
                        case 4:
                            driverApplicationIsRunning = false;
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

    // print menu for driver
    private static void printMenu() {
        System.out.println("Driver, what would you like to do?");
        System.out.println("1. Search requests");
        System.out.println("2. Take a request");
        System.out.println("3. Finish a trip");
        System.out.println("4. Go back");
        System.out.println("Please enter [1-4].");
    }

    /**
     * search request - done
     */
    private static void searchRequest() {
        int driverId = getDriverId();
        String[] location = getDriverLocation().split(" ");
        int maximumDistance = getMaximumDistance();

        String mysqlStatement = String.format(
            "select r.id, p.name, r.passengers, r.start_location, r.destination " +
            "from request r, passenger p, taxi_stop start_location, vehicle v, driver d " +
            "where r.taken=false and r.passenger_id=p.id and d.id=%d and d.vehicle_id=v.id and r.start_location=start_location.name and " + 
            "(abs(%d-start_location.location_x) + abs(%d-start_location.location_y) <= %d) and v.seats>=r.passengers and d.driving_years>=r.driving_years and lower(v.model) regexp if(r.model=\"\", \".*\", r.model)" +
            "order by r.id asc",
            driverId, Integer.parseInt(location[0]), Integer.parseInt(location[1]), maximumDistance
        );

        try {
            ResultSet requests = DatabaseConnection.executeQuery(mysqlStatement);

            if(!requests.isBeforeFirst()) {
                System.err.println(Error.NO_REQUEST_FOUND);
            } else {
                System.out.println("request ID, passenger name, num of passengers, start location, destination");
                while(requests.next()) {
                    System.out.print(requests.getInt(1) + ", ");
                    System.out.print(requests.getString(2) + ", ");
                    System.out.print(requests.getInt(3) + ", ");
                    System.out.print(requests.getString(4) + ", ");
                    System.out.print(requests.getString(5) + "\n");
                }
            }
        } catch(SQLException err) {
            err.printStackTrace();
        }
    }

    /**
     * take request - done
     */
    private static void takeRequest() {

        int driverId = getDriverId();

        try {
            // check if driver has unfinished trip
            String checkUnfinishedTripSQL = String.format(
                "select count(*) " +
                "from trip " +
                "where driver_id=%d and end_time is null and fee is null",
                driverId
            );
            ResultSet unfinishedTrip = DatabaseConnection.executeQuery(checkUnfinishedTripSQL);

            unfinishedTrip.next();

            if(unfinishedTrip.getInt(1) >= 1) {
                System.err.println(Error.DRIVER_HAS_UNFINSHED_TRIP);
            } else {
                int requestId = getRequestId();

                String checkRequirements = String.format(
                    "select r.passenger_id, r.start_location, r.destination, r.taken " +
                    "from request r, driver d, vehicle v " +
                    "where r.id=%d and d.vehicle_id=v.id and v.seats>=r.passengers and lower(v.model) regexp if(r.model=\"\", \".*\", r.model) and d.driving_years>=r.driving_years",
                    requestId
                );

                ResultSet request = DatabaseConnection.executeQuery(checkRequirements);

                if(!request.isBeforeFirst()) {
                    System.out.println(Error.REQUIREMENT_NOT_MATCH);
                } else {

                    request.next();
                    if(request.getBoolean(4)) {
                        System.err.println(Error.REQUEST_HAS_BEEN_TAKEN);
                    } else {
                        // create a trip
                        try {
                            Connection connection = DatabaseConnection.connect();
            
                            // create a trip 
                            String mysqlStatement = "insert into trip(driver_id, passenger_id, start_location, destination, start_time) values(?, ?, ?, ?, ?)";
            
                            // prepare statement for trip
                            PreparedStatement statement = connection.prepareStatement(mysqlStatement);

                            int passengerId = request.getInt(1);
                            String startLocation = request.getString(2);
                            String destination = request.getString(3);
                            String startTime = getCurrentTimeStamp();
            
                            statement.setInt(1, driverId);
                            statement.setInt(2, passengerId);
                            statement.setString(3, startLocation);
                            statement.setString(4, destination);
                            statement.setString(5, startTime);
                            
                            int rowAffected = statement.executeUpdate();
                            if(rowAffected > 0) {
                                String tripStatement = String.format(
                                    "select t.id, p.name, t.start_time " + 
                                    "from trip t, passenger p " +
                                    "where t.passenger_id=p.id and p.id=%d and \"%s\"=t.start_time",
                                    passengerId, startTime
                                );

                                ResultSet trip = DatabaseConnection.executeQuery(tripStatement);

                                trip.next();

                                System.out.println("Trip ID, Passenger name, Start");
                                System.out.print(trip.getInt(1) + ", ");
                                System.out.print(trip.getString(2) + ", ");
                                System.out.print(trip.getString(3) + "\n");

                                // update request status taken
                                String updateStatus = "update request set taken=true where id=?";
                                
                                PreparedStatement updateStatement = connection.prepareStatement(updateStatus);

                                updateStatement.setInt(1, requestId);

                                updateStatement.executeUpdate();
                            }
                        } catch(Exception err) {
                            System.out.println(Error.QUERY_FAILURE);
                        }
                    }
                }
            }
        } catch (Exception err) {
            System.err.println(Error.QUERY_FAILURE);
        }
    }

    /**
     * finish trip - done
     */
    private static void finishTrip() {
        System.out.println("Finish Trip");
        int driverId = getDriverId();

        try {
            // get current unfinished trip information
            String currentUnfinishedTrip = String.format(
                "select t.id, t.passenger_id, date_format(t.start_time, \"%s\"),p.name " +
                "from trip t, passenger p " +
                "where driver_id=%d and t.passenger_id=p.id and (end_time is null) and (fee is null)",
                "%Y-%m-%d %T", driverId
            );

            ResultSet trip = DatabaseConnection.executeQuery(currentUnfinishedTrip);

            if(!trip.isBeforeFirst()) {
                System.err.println(Error.UNFINISHED_TRIP_NOT_FOUND);
            } else {
                trip.next();
                int tripId = trip.getInt(1);
                int passengerId = trip.getInt(2);
                String passengerName = trip.getString(4);

                String startTime = trip.getString(3);
                String endTime = getCurrentTimeStamp();
                long fee = ((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime)).getTime() - 
                            (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime)).getTime())/(1000 * 60);


                System.out.println("Trip ID, Passenger ID, Start");
                System.out.print(tripId + ", ");
                System.out.print(passengerId + ", ");
                System.out.print(startTime + "\n");

                boolean respondIsValid = false;

                while(!respondIsValid) {
                    System.out.println("Do you wish to finish the trip? [y/n]");
                    String driverRespond = Main.scanner.nextLine().toLowerCase();

                    if(driverRespond.compareTo("y") == 0) {
                        Connection connection = DatabaseConnection.connect();

                        String updateTripSQL = "update trip set end_time=?, fee=? where id=?";

                        PreparedStatement updateTripStatement = connection.prepareStatement(updateTripSQL);

                        updateTripStatement.setString(1, endTime);
                        updateTripStatement.setInt(2, (int)fee);
                        updateTripStatement.setInt(3, tripId);

                        int rowUpdated = updateTripStatement.executeUpdate();

                        if(rowUpdated > 0) {
                            System.out.println("Trip ID, Passenger name, Start, End, Fee");
                            System.out.println(tripId + ", " + passengerName + ", " + startTime + ", " + endTime + ", " + fee);
                        }

                        respondIsValid = true;
                    } else if(driverRespond.compareTo("n") == 0) {
                        respondIsValid = true;
                    } else {
                        System.err.println(Error.INVALID_INPUT);
                    }
                }
            }
        } catch(SQLException err) {
            System.err.println(Error.QUERY_FAILURE);
        } catch(InputMismatchException err) {
            System.err.println(Error.INVALID_INPUT);
        } catch(ParseException err) {
            System.out.println(Error.INVALID_DATE_FORMAT);
        }
    }

    private static int getDriverId() {

        boolean inputIsValid = false;
        int id = 0;

        while(!inputIsValid) {
            try {
                System.out.println("Please enter your ID.");
                id = Main.scanner.nextInt();

                // check whether ID exist in database
                String mysqlStatement = String.format(
                    "select id " + 
                    "from driver " + 
                    "where id = %d ", id);

                ResultSet idInDatabase = DatabaseConnection.executeQuery(mysqlStatement);

                if(!idInDatabase.isBeforeFirst()) {
                    System.out.println(Error.DRIVER_ID_NOT_FOUND);
                } else {
                    inputIsValid = true;
                }
            } catch (InputMismatchException err) {
                System.err.println(Error.INVALID_INPUT);
                Main.scanner.next();
            } catch(SQLException err) {
                err.printStackTrace();
            }
        }
        return id;
    }

    private static String getDriverLocation() {
        boolean inputIsValid = false;
        String driverLocation = "";

        while(!inputIsValid) {
            try {
                System.out.println("Please enter the coordinates of your location.");
                driverLocation = Main.scanner.nextLine();

                int spaces = driverLocation.length() - driverLocation.replace(" ", "").length();
                
                if(spaces != 1) {
                    System.err.println(Error.DRIVER_LOCATION_FORMAT_INVALID);
                } else {
                    String[] location = driverLocation.split(" ");
                    int location_x = Integer.parseInt(location[0]);
                    int location_y = Integer.parseInt(location[1]);

                    inputIsValid = true;
                }

            } catch(NumberFormatException err) {
                System.err.println(Error.DRIVER_LOCATION_FORMAT_INVALID);
            } catch(Exception err) {
                System.err.println(Error.DRIVER_LOCATION_FORMAT_INVALID);
            } 
        }

        return driverLocation;
    }

    private static int getMaximumDistance() {
        boolean inputIsValid = false;
        int maximumDistance = 0;

        while(!inputIsValid) {
            System.out.println("Please enter the maximum distance from you to passenger.");
            try {
                maximumDistance = Main.scanner.nextInt();

                inputIsValid = true;
            } catch (InputMismatchException err) {
                System.err.println(Error.INVALID_INPUT);
                Main.scanner.next();
            }
        }
        return maximumDistance;
    }

    private static int getRequestId() {
        boolean inputIsValid = false;
        int requestId = 0;
        while(!inputIsValid) {
            try {
                System.out.println("Please enter the request ID.");
                requestId = Main.scanner.nextInt();

                // check whether ID exist in database
                String mysqlStatement = String.format(
                    "select id " + 
                    "from request " + 
                    "where id = %d ", requestId);

                ResultSet requestIdInDatabase = DatabaseConnection.executeQuery(mysqlStatement);

                if(!requestIdInDatabase.isBeforeFirst()) {
                    System.out.println(Error.REQUEST_ID_NOT_FOUND);
                } else {
                    inputIsValid = true;
                }
            } catch (InputMismatchException err) {
                System.err.println(Error.INVALID_INPUT);
                Main.scanner.next();
            } catch(SQLException err) {
                System.err.println(Error.SQL_EXCEPTION);
                System.err.println(err.getMessage());
            }
        }
        return requestId;
    }

    private static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
