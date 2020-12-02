import java.util.*;
import java.sql.*;
import java.io.*;
import java.nio.file.Paths;


public class Manager extends Object {
    
    
    static Scanner integerScanner = new Scanner(System.in);
    static Scanner stringScanner = new Scanner(System.in);
    
    /*
    The distance between two locations is measured by Manhattan distance, i.e., the distance between (x1, y1) and (x2, y2) is |x1 - x2| + |y1 - y2|
    
    In this case, distance = destination (x1, y1) - start location (x2, y2)
    
    */
    

     public static void run(){
         
         
                 boolean managerApplicationIsRunning = true;
                 
                 while(managerApplicationIsRunning) {
            printManagerMenu();
                 
                 boolean inputIsValid = false;
                 
                 
        while(!inputIsValid) {
                try {
                    int managerAction = integerScanner.nextInt();
    
                    switch(managerAction) {
                        case 1:
                            findTrips();
                            inputIsValid = true;
                            break;
                        case 2:
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
     
     //menu print for manager
    
     private static void printManagerMenu() {
        System.out.println("Manager, what would you like to do?");
        System.out.println("1. Find trips");
        System.out.println("2. Go back");
        System.out.println("Please enter [1-2].");
        
    }
    
    //find trips for manager
    
    //have to define first i think, the distance formula mentioned before
    
    private static void findTrips(){
        
             System.out.println("Please enter the minimum traveling distance.");


                System.out.println("Please enter the maximum traveling distance.");

    }
    
     
     
     
     
     
     
            System.out.println("trip id, driver name, passenger name, start location, destination, duration");
    
    
                        }




