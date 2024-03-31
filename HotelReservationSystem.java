import java.sql.*;
import java.util.Scanner;
import java.util.SortedMap;

public class HotelReservationSystem {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Nidhu@0905";
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
        }
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            while (true){
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1. Reserve a Room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                switch (choice){
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservation(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid Choice. Try Again.");
                }
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    private static void reserveRoom(Connection connection, Scanner scanner){
        try {
            System.out.print("Enter Guest Name: ");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.print("Enter Room Number: ");
            int roomNumber = scanner.nextInt();
            System.out.print("Enter Contact Number: ");
            String contactNumber = scanner.next();

            String sql = "INSERT INTO reservations (guest_name, room_number, contact_number)"+
                    "VALUES ('"+guestName+"', "+roomNumber+", '"+contactNumber+"')";
            try (Statement statement = connection.createStatement()){
                //exceuteUpdate() Function Only use for Insert, Update, Delete Data. (this Return interger Value)
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows>0){
                    System.out.println("Reservation Successful!");
                }else {
                    System.out.println("Reservation Failed.");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private static void viewReservation(Connection connection) throws SQLException{
        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)){
            //exceuteQuery() Function Only use for Retrieve Data. (this return Data)
            System.out.println("Current Reservations:");
            System.out.println("+----------------+------------------+---------------+----------------------+-----------------------+");
            System.out.println("| Reservation ID | Guest            | Room Number   | Contact Number       | Reservation Date      |");
            System.out.println("+----------------+------------------+---------------+----------------------+-----------------------+");
            while (resultSet.next()){
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                //formate and display the reservation date in a table line format
                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-21s  |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println("+----------------+------------------+---------------+----------------------+-----------------------+");
        }
    }
    private static void getRoomNumber(Connection connection, Scanner scanner){
        try{
            System.out.print("Enter reservation ID: ");
            int reservationId = scanner.nextInt();
            System.out.print("Enter Guest Name : ");
            String  guestName = scanner.next();

            String sql = "SELECT room_number FROM reservations" +
                    "WHERE reservation_id = " + reservationId +
                    "AND guest_name = '" + guestName + "'";

            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql) ){

                if (resultSet.next()){
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room Number For Reservation Id "+ reservationId +
                            " and Guest "+ guestName + " is: "+roomNumber);
                }else {
                    System.out.println("Reservation Not Found for the given ID and Guest name.");
                }

            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private static void updateReservation(Connection connection, Scanner scanner){
        try{
            System.out.print("Enter Reservation Id to Update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); //Consume the NextLine Character

            if (!reservationExists(connection, reservationId)){
                System.out.println("Reservation not found for the given to.");
                return;
            }
            System.out.print("Enter Guest Name: ");
            String newGuestName = scanner.next();
            scanner.nextLine();
            System.out.print("Enter Room Number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.print("Enter Contact Number: ");
            String newContactNumber = scanner.next();

            String sql = "UPDATE reservation SET guest_name = '"+newGuestName + "', "+
                    "room_number = "+ newRoomNumber+", "+
                    "contact_number = '"+ newContactNumber + "', "+
                    "WHERE reservation_id = "+reservationId;
            try(Statement statement = connection.createStatement()){
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows>0){
                    System.out.println("Reservation Updated Successfully!");
                }else {
                    System.out.println("Reservation Update Failed.");
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void deleteReservation(Connection connection, Scanner scanner){
        try {
            System.out.print("Enter Reservation ID to Delete: ");
            int reservationId = scanner.nextInt();
            if(!reservationExists(connection, reservationId)){
                System.out.println("Reservation not Found for the given ID. ");
                return;
            }
            String sql = "DELETE FROM reservations WHERE reservation_id = "+reservationId;
            try(Statement statement = connection.createStatement()){
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows>0){
                    System.out.println("Reservation Deleted Successfully!");
                }else {
                    System.out.println("Reservation Deleted Failed.");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private static boolean reservationExists(Connection connection, int reservationId){
        try{
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = "+reservationId;
            try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)){
                return resultSet.next();
                //If there's a result, the reservation exists
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
            //Handle Database Error
        }
    }
    private static void exit() throws InterruptedException{
        System.out.print("Exiting System");
        int i = 5;
        while (i!=0){
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
        System.out.println("Thank You! For Using Hotel Reservation System.");
    }
}