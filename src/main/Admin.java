import java.util.Scanner;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;
import java.io.*;

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
        String checkNumberOfTables = "SELECT COUNT(DISTINCT `table_name`) AS TotalNumberOfTables FROM `information_schema`.`columns` WHERE `table_schema` = 'group69';";

        try{
            ResultSet numberOfTables = DatabaseConnection.executeQuery(checkNumberOfTables);
            System.out.println(numberOfTables);


            System.out.println("Processing...Done! Tables are deleted");

        } catch(Exception err) {
            System.out.println(Error.QUERY_FAILURE);
        }

        // driver table
        /* String createDriverTable = "CREATE TABLE test(
            id integer unsigned not null auto_increment,
            name varchar(30) not null,
            vehicle_id varchar(6) not null,
            driving_years integer unsigned,
            primary key(id),
            foreign key(vehicle_id) references vehicle(id)
        );";
        // passenger table
        String createPassengerTable = "CREATE TABLE passenger (
            id integer unsigned not null auto_increment,
            name varchar(30) not null,
            primary key (id)
        );";
        // request table
        String createRequestTable = "CREATE TABLE request(
            id integer unsigned not null auto_increment,
            passenger_id integer unsigned,
            start_location varchar(20) not null,
            destination varchar(20) not null,
            model varchar(30) not null,
            passengers integer unsigned,
            taken boolean default false,
            driving_years int unsigned,
            primary key(id),
            foreign key(passenger_id) references passenger(id),
            foreign key(start_location) references taxi_stop(name),
            foreign key(destination) references taxi_stop(name)
        );";
        // taxi_stop table
        String createTaxiStopTable = "CREATE TABLE taxi_stop(
            name varchar(20) not null,
            location_x integer,
            location_y integer,
            primary key	(name)
        );";
        // trip table
        String createTripTable = "CREATE TABLE trip (
            id integer unsigned not null auto_increment,
            driver_id integer unsigned,
            passenger_id integer unsigned,
            start_location varchar(20) not null,
            destination varchar(20) not null,
            start_time DATE,
            end_time DATE,
            fee integer,
            primary key(id),
            foreign key(driver_id) references driver(id),
            foreign key(passenger_id) references passenger(id),
            foreign key(start_location)	references taxi_stop(name),
            foreign key(destination) references taxi_stop(name)
        );";

        // vehicle table
        String createVehicleTable = "CREATE TABLE vehicle(
            id varchar(6) not null,
            model varchar(30) not null,
            seats integer unsigned,
            primary key(id)
        );"; */
    }

    // delete tables in the database
    public static void deleteTables() {
        String[] tableNames = {"driver", "passenger", "request", "taxi_stop", "trip", "vehicle"};
        
        for (String tableName: tableNames) {
            deleteTable(tableName);
        }

        System.out.println("Processing...Done! Tables are deleted");
    }

    // delete table based on name
    public static void deleteTable(String tableName) {
        try{
            String mysqlStatement = "drop table if exists " + tableName;
            Connection con = DatabaseConnection.connect();
            Statement stmt = con.createStatement();
            stmt.executeUpdate(mysqlStatement);
        } catch(Exception err) {
            System.out.println(Error.QUERY_FAILURE);
        }
    }

    // load data from a folder containing 4 data files (drivers, vehicles, passengers, and trips, all in .csv format)
    public static void loadData() {
        
    }

    // check the data in the database, shows the number of records in each table
    public static void checkData() {
        
    }
}

