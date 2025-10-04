package experiment7;

import java.sql.*;
import java.util.Scanner;

public class HostelSystem {

    static final String URL = "jdbc:mysql://localhost:3306/HostelDB";
    static final String USER = "root";
    static final String PASSWORD = "Aditya@2003"; // Change if needed

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner sc = new Scanner(System.in)) {

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connected to HostelDB!");

            while (true) {
                System.out.println("\n--- Hostel Management System ---");
                System.out.println("1. Student Login");
                System.out.println("2. Admin Login");
                System.out.println("3. Exit");
                System.out.print("Choose: ");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 -> studentMenu(con, sc);
                    case 2 -> adminMenu(con, sc);
                    case 3 -> {
                        System.out.println("Exiting... Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------- Student Menu -----------
    private static void studentMenu(Connection con, Scanner sc) throws SQLException {
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        String sql = "SELECT * FROM Student WHERE Username=? AND Password=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, password);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int studentId = rs.getInt("StudentID");
                    System.out.println("Login successful! Welcome " + rs.getString("Name"));

                    while (true) {
                        System.out.println("\n-- Student Menu --");
                        System.out.println("1. Submit Request");
                        System.out.println("2. View My Requests");
                        System.out.println("3. Logout");
                        System.out.print("Choose: ");
                        int choice = sc.nextInt();
                        sc.nextLine();

                        switch (choice) {
                            case 1 -> submitRequest(con, sc, studentId);
                            case 2 -> viewRequests(con, studentId);
                            case 3 -> { System.out.println("Logged out."); return; }
                            default -> System.out.println("Invalid choice!");
                        }
                    }
                } else {
                    System.out.println("Invalid username or password!");
                }
            }
        }
    }

    private static void submitRequest(Connection con, Scanner sc, int studentId) throws SQLException {
        System.out.print("Enter your request: ");
        String text = sc.nextLine();

        String sql = "INSERT INTO Requests (StudentID, RequestText) VALUES (?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setString(2, text);
            pst.executeUpdate();
            System.out.println("Request submitted successfully!");
        }
    }

    private static void viewRequests(Connection con, int studentId) throws SQLException {
        String sql = "SELECT * FROM Requests WHERE StudentID=? ORDER BY RequestDate DESC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                System.out.println("RequestID | Text | Status | Date");
                System.out.println("--------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%d | %s | %s | %s%n",
                            rs.getInt("RequestID"),
                            rs.getString("RequestText"),
                            rs.getString("Status"),
                            rs.getTimestamp("RequestDate"));
                }
            }
        }
    }

    // ----------- Admin Menu -----------
    private static void adminMenu(Connection con, Scanner sc) throws SQLException {
        System.out.print("Admin Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        String sql = "SELECT * FROM Admin WHERE Username=? AND Password=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, password);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Admin login successful!");

                    while (true) {
                        System.out.println("\n-- Admin Menu --");
                        System.out.println("1. View All Requests");
                        System.out.println("2. Update Request Status");
                        System.out.println("3. Logout");
                        System.out.print("Choose: ");
                        int choice = sc.nextInt();
                        sc.nextLine();

                        switch (choice) {
                            case 1 -> viewAllRequests(con);
                            case 2 -> updateRequestStatus(con, sc);
                            case 3 -> { System.out.println("Logged out."); return; }
                            default -> System.out.println("Invalid choice!");
                        }
                    }
                } else {
                    System.out.println("Invalid admin credentials!");
                }
            }
        }
    }

    private static void viewAllRequests(Connection con) throws SQLException {
        String sql = "SELECT r.RequestID, s.Name, r.RequestText, r.Status, r.RequestDate " +
                     "FROM Requests r JOIN Student s ON r.StudentID = s.StudentID " +
                     "ORDER BY r.RequestDate DESC";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("ID | Student | Text | Status | Date");
            System.out.println("-----------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s | %s%n",
                        rs.getInt("RequestID"),
                        rs.getString("Name"),
                        rs.getString("RequestText"),
                        rs.getString("Status"),
                        rs.getTimestamp("RequestDate"));
            }
        }
    }

    private static void updateRequestStatus(Connection con, Scanner sc) throws SQLException {
        System.out.print("Enter Request ID to update: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter new status (Pending/Approved/Rejected): ");
        String status = sc.nextLine();

        String sql = "UPDATE Requests SET Status=? WHERE RequestID=?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            int rows = pst.executeUpdate();
            System.out.println(rows + " request updated successfully!");
        }
    }
}

