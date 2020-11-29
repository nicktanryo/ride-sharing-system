public class Error extends Object{

    // database
    static String MYSQL_DRIVER_NOT_FOUND = "[ERROR]: Java MySQL DB Driver not found!!";
    static String QUERY_FAILURE = "[ERROR] Unable to make query";
    static String SQL_EXCEPTION = "[ERROR] SQL Exception";
    static String SQL_TIMEOUT_EXCEPTION = "[ERROR] SQL Timeout Exception";

    // general input
    static String INVALID_INPUT = "[ERROR] Invalid Input";

    // admin
    static String DUPLICATE_ENTRY = "[ERROR] Duplicate entry with same primary key found";

    // passanger
    static String PASSENGER_HAS_OPEN_REQUEST = "[ERROR] Passenger has an open request";
    static String PASSENGER_ID_NOT_FOUND = "[ERROR] Passenger id not found";
    static String INVALID_NUMBER_OF_PASSENGER = "[ERROR] Invalid number of passengers.";

    // taxi stops
    static String START_LOCATION_NOT_FOUND = "[ERROR] Start Location not found.";
    static String DESTINATION_NOT_FOUND = "[ERROR] Destination not found.";
    static String SAME_START_LOCATION_AND_DESTIONATION = "[ERROR] Destination and start location should be different";

    // date input constraint
    static String INVALID_DATE_FORMAT = "[ERROR] Invalid Date Format.";
    static String INVALID_START_AND_END_DATE = "[ERROR] End Date should be greater than Start Date";

    // vehicle
    static String CAR_MODEL_NOT_FOUND = "[ERROR] Car model not found";
    static String DRIVER_NOT_FOUND = "[ERROR] No drivers found, please adjust the criteria";

    // trip
    static String TRIPS_NOT_FOUND = "[ERROR] No trips found";

    // driver
    static String DRIVER_ID_NOT_FOUND = "[ERROR] Driver id not found";
    static String DRIVER_LOCATION_FORMAT_INVALID = "[ERROR] Driver location format invalid";
    static String NO_REQUEST_FOUND = "[ERROR] No request found";
    static String LOCATION_FORMAT = "Location format: \"location_x location_y\"";
    static String UNFINISHED_TRIP_NOT_FOUND = "[ERROR] Unfinished trip not found";
    static String DRIVER_HAS_UNFINSHED_TRIP = "[ERROR] Driver has unfinished trip, Please complete the unfinished trip first";

    // request
    static String REQUEST_ID_NOT_FOUND = "[ERROR] Request Id not found";
    static String REQUIREMENT_NOT_MATCH = "[ERROR] Driver's attirbutes do not match request requirements";
    static String REQUEST_HAS_BEEN_TAKEN = "[ERROR] Request has been taken";
}
