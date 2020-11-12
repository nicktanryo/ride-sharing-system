import java.sql.*;

public class DatabaseConnection{
    public static Connection connect() {
        String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/group69";
        String dbUsername = "Group69";
        String dbPassword = "egahomo123";
    
        Connection con = null;
    
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);

            // System.out.println("CONNECTED");
        } catch (ClassNotFoundException e){
            System.out.println(Error.MYSQL_DRIVER_NOT_FOUND);
            System.exit(0);
        } catch(SQLException e) {
            System.out.println(e);
        }

        return con;
    }
}


