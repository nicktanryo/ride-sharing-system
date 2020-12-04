import java.util.*;
import java.sql.*;
import java.io.*;
import java.nio.file.Paths;

public class Admin extends Object {

    static Scanner integerScanner = new Scanner(System.in);
    static Scanner stringScanner = new Scanner(System.in);

    // main function for admin
    public static void run() {

        boolean runningAdminMenu = true;

        while(runningAdminMenu) {
            printAdminMenu();

            boolean inputIsValid = false;

            while(!inputIsValid) {
                try {
                    int adminAction = integerScanner.nextInt();

                    switch(adminAction) {
                        case 1:
                            createTables();
                            inputIsValid = true;
                            break;
                        case 2:
                            deleteTables();
                            inputIsValid = true;
                            break;
                        case 3:
                            loadData();
                            inputIsValid = true;
                            break;
                        case 4:
                            checkData();
                            inputIsValid = true;
                            break;
                        case 5:
                            runningAdminMenu = false;
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

    // printing admin menu
    public static void printAdminMenu() {
        System.out.println("Administrator, what would you like to do?");
        System.out.println("1. Create tables");
        System.out.println("2. Delete tables");
        System.out.println("3. Load data");
        System.out.println("4. Check data");
        System.out.println("5. Go back");
        System.out.println("Please enter [1-5]");
    }

    // create tables in the database
    public static void createTables() {
        // check whether database is empty
        String checkNumberOfTables = "SELECT COUNT(*) FROM information_schema.tables WHERE `table_schema` = 'group69';";

        try{
            ResultSet resultSet = DatabaseConnection.executeQuery(checkNumberOfTables);
            resultSet.next();
            String numberOfTables = resultSet.getString(1);

            if (Integer.parseInt(numberOfTables) > 0) {
                System.out.println("Process failed! There are existing tables");
            } else {
                // create queries
                String createDriverTable = "create table driver(id integer unsigned not null auto_increment, name varchar(30) not null, vehicle_id varchar(6) not null, driving_years integer unsigned, primary key(id), foreign key(vehicle_id) references vehicle(id));";
                String createRequestTable = "create table request(id integer unsigned not null auto_increment, passenger_id integer unsigned, start_location varchar(20) not null, destination varchar(20) not null, model varchar(30) not null, passengers integer unsigned, taken boolean default false, driving_years int unsigned, primary key(id), foreign key(passenger_id) references passenger(id), foreign key(start_location) references taxi_stop(name), foreign key(destination) references taxi_stop(name));";
                String createTaxiStopTable = "create table taxi_stop(name varchar(20) not null, location_x integer, location_y integer, primary key (name));";
                String createTripTable = "create table trip(id integer unsigned not null auto_increment, driver_id integer unsigned, passenger_id integer unsigned, start_location varchar(20) not null, destination varchar(20) not null, start_time DATETIME, end_time DATETIME, fee integer, primary key(id), foreign key(driver_id) references driver(id), foreign key(passenger_id) references passenger(id), foreign key(start_location)  references taxi_stop(name), foreign key(destination) references taxi_stop(name));";
                String createVehicleTable = "create table vehicle(id varchar(6) not null, model varchar(30) not null, seats integer unsigned, primary key(id));";
                String createPassengerTable = "create table passenger(id integer unsigned not null auto_increment, name varchar(30) not null, primary key (id));";

                // prepare connection
                Connection con = DatabaseConnection.connect();
                Statement stmt = con.createStatement();

                // execute query
                stmt.executeUpdate(createPassengerTable);
                stmt.executeUpdate(createTaxiStopTable);
                stmt.executeUpdate(createVehicleTable);
                stmt.executeUpdate(createDriverTable);
                stmt.executeUpdate(createRequestTable);
                stmt.executeUpdate(createTripTable);
                
                

                System.out.println("Processing...Done! Tables are created!");
            }

        } catch(Exception err) {
            System.out.println(Error.QUERY_FAILURE);
        }
    }
    // delete tables in the database
    public static void deleteTables() {
        try{

            // get table names
            String sqlStatement = "show tables;";
            ResultSet rs= DatabaseConnection.executeQuery(sqlStatement);
            String[] tableNames = new String[20];
            int count = 0;
            while(rs.next()){
                tableNames[count] = rs.getString(1);
                count++;
            }

            // prepare connection
            Connection con = DatabaseConnection.connect();
            Statement stmt = con.createStatement();
            String mysqlStatement;

            // disable foreign key checks
            stmt.executeUpdate("set foreign_key_checks=0;");

            // loop through all the table names and delete them
            for (int i = 0; i < count; i++) {
                mysqlStatement = "drop table if exists " + tableNames[i] + ";";
                stmt.executeUpdate(mysqlStatement);
            }

            // enable foreign key checks
            stmt.executeUpdate("set foreign_key_checks=1;");

            System.out.println("Processing...Done! Tables are deleted");
            
        } catch(Exception err) {
            System.out.println(Error.QUERY_FAILURE);
        }
    }

    // load data from a folder containing 5 data files (drivers, vehicles, passengers, taxi_stops, and trips, all in .csv format)
    public static void loadData() {
        System.out.println("Please enter the folder path");
        String folderPath = Paths.get(".").toAbsolutePath().normalize().toString() + "/" + stringScanner.next();

        loadPassengersData(folderPath);
        loadTaxiStopsData(folderPath);
        loadVehiclesData(folderPath);
        loadDriversData(folderPath);
        loadTripsData(folderPath);
    }
    public static void loadPassengersData(String folderPath) {
        String csvFile = folderPath + "/passengers.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try{
            br = new BufferedReader(new FileReader(csvFile));

            Connection connection = DatabaseConnection.connect();
            String insertQuery = "insert into passenger(id, name) values(?, ?);";
            PreparedStatement statement = connection.prepareStatement(insertQuery);

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] passenger = line.split(cvsSplitBy);
            
                statement.setInt(1, Integer.parseInt(passenger[0]));
                statement.setString(2, passenger[1]);
                            
                statement.executeUpdate();
            }
            System.out.println("Passengers added");
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println(Error.DUPLICATE_ENTRY);
        }
    }

    public static void loadTaxiStopsData(String folderPath) {
        String csvFile = folderPath + "/taxi_stops.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try{
            br = new BufferedReader(new FileReader(csvFile));

            Connection connection = DatabaseConnection.connect();
            String insertQuery = "insert into taxi_stop(name, location_x, location_y) values(?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(insertQuery);

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] taxi_stop = line.split(cvsSplitBy);
            
                statement.setString(1, taxi_stop[0]);
                statement.setInt(2, Integer.parseInt(taxi_stop[1]));
                statement.setInt(3, Integer.parseInt(taxi_stop[2]));
                            
                statement.executeUpdate();
            }
            System.out.println("Taxi stops added");
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println(Error.DUPLICATE_ENTRY);
        }
    }

    public static void loadVehiclesData(String folderPath) {
        String csvFile = folderPath + "/vehicles.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try{
            br = new BufferedReader(new FileReader(csvFile));

            Connection connection = DatabaseConnection.connect();
            String insertQuery = "insert into vehicle(id, model, seats) values(?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(insertQuery);

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] vehicle = line.split(cvsSplitBy);
            
                statement.setString(1, vehicle[0]);
                statement.setString(2, vehicle[1]);
                statement.setInt(3, Integer.parseInt(vehicle[2]));
                            
                statement.executeUpdate();
            }
            System.out.println("Vehicles added");
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println(Error.DUPLICATE_ENTRY);
        }
    }
    public static void loadDriversData(String folderPath) {
        String csvFile = folderPath + "/drivers.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try{
            br = new BufferedReader(new FileReader(csvFile));

            Connection connection = DatabaseConnection.connect();
            String insertQuery = "insert into driver(id, name, vehicle_id, driving_years) values(?, ?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(insertQuery);

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] driver = line.split(cvsSplitBy);
            
                statement.setInt(1, Integer.parseInt(driver[0]));
                statement.setString(2, driver[1]);
                statement.setString(3, driver[2]);
                statement.setInt(4, Integer.parseInt(driver[3]));
                            
                statement.executeUpdate();
            }
            System.out.println("Drivers added");
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println(Error.DUPLICATE_ENTRY);
        }
    }
    public static void loadTripsData(String folderPath) {
        String csvFile = folderPath + "/trips.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try{
            br = new BufferedReader(new FileReader(csvFile));

            Connection connection = DatabaseConnection.connect();
            String insertQuery = "insert into trip(id, driver_id, passenger_id, start_location, destination, start_time, end_time, fee) values(?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(insertQuery);

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] trip = line.split(cvsSplitBy);
            
                statement.setInt(1, Integer.parseInt(trip[0]));
                statement.setInt(2, Integer.parseInt(trip[1]));
                statement.setInt(3, Integer.parseInt(trip[2]));
                statement.setString(4, trip[5]);
                statement.setString(5, trip[6]);
                statement.setString(6, trip[3]);
                statement.setString(7, trip[4]);
                statement.setInt(8, Integer.parseInt(trip[7]));
                            
                statement.executeUpdate();
            }
            System.out.println("Trips added");
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println(Error.DUPLICATE_ENTRY);
        }
    }

    // check the data in the database, shows the number of records in each table
    public static void checkData() {
        try{
            // get table names
            // String[] tableNames = new String[20];
            // String sqlStatement = "show tables;";
            // ResultSet rs= DatabaseConnection.executeQuery(sqlStatement);
            // int count = 0;

            // while(rs.next()){
            //     tableNames[count] = rs.getString(1);
            //     count++;
            // }
            String[] tableNameShown = {"Vehicle", "Passenger", "Driver", "Trip", "Request", "Taxi_Stop"};
            String[] tableNames = {"vehicle", "passenger", "driver", "trip", "request", "taxi_stop"};
            int count = 6;

            // loop through all the table names print the number of records
            ResultSet rsRecordCount;
            String countRecordStatement;

            for (int i = 0; i < count; i++) {
                countRecordStatement = "select count(*) from " + tableNames[i] + ";";
                rsRecordCount = DatabaseConnection.executeQuery(countRecordStatement);
                rsRecordCount.next();
                String recordCountString = rsRecordCount.getString(1);
                System.out.println(tableNameShown[i] + ": " + recordCountString);
            }

        } catch(Exception err) {
            System.out.println(Error.QUERY_FAILURE);
        }
        
    }
}

