import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        boolean applicationIsRunning = true;
        Scanner scanner = new Scanner(System.in);

        while(applicationIsRunning) {
            printMenu();

            boolean inputIsValid = false;

            while (!inputIsValid) {
                try {

                    int userAccount =  scanner.nextInt();

                    switch(userAccount) {
                        case 1:
                            // handleAdministrator();
                            inputIsValid = true;
                            break;
                        case 2:
                            Passenger.run();
                            inputIsValid = true;
                            break;
                        case 3:
                            Driver.run();
                            inputIsValid = true;
                            break;
                        case 4:
                            // handleManager();
                            inputIsValid = true;
                            break;
                        case 5:
                            applicationIsRunning = false;
                            inputIsValid = true;
                            break;
                        default:
                            System.err.println(Error.INVALID_INPUT);
                    }

                } catch (Exception e) {

                    System.err.println(Error.INVALID_INPUT);
                    scanner.next();
                    
                }
            }
        }
        scanner.close();
    }

    public static void printMenu() {
        System.out.println("Welcome! Who are you?");
        System.out.println("1. An Administrator");
        System.out.println("2. A passenger");
        System.out.println("3. A driver");
        System.out.println("4. A manager");
        System.out.println("5. None of the above");
        System.out.println("Please enter [1-4]");
    }
}
