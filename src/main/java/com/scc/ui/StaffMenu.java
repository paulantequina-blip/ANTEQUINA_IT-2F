package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.DatabaseConnection;
import com.scc.dao.StaffBookingDao;
import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Staff dashboard. Needs only:
 *   StaffBookingDao, DatabaseConnection, ActivityLogDao, User.
 */
public class StaffMenu {

    private final Scanner scanner;
    private final StaffBookingDao dao;

    public StaffMenu(Scanner scanner) {
        this.scanner = scanner;
        this.dao = new StaffBookingDao();
    }

    // =====================================
    // MAIN STAFF MENU
    // =====================================
    public void display(User staff) {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("       STUDY SPACE BOOKING SYSTEM");
            System.out.println("========================================");
            System.out.println("       LIBRARIAN / STAFF DASHBOARD");
            System.out.println("========================================");
            System.out.println("Welcome, " + staff.fullName());
            System.out.println("----------------------------------------");
            System.out.println("[1] View All Bookings");
            System.out.println("[2] Approve / Reject Bookings");
            System.out.println("[3] Manage Bookings");
            System.out.println("[4] Check-out Students");
            System.out.println("[5] Mark No-Show");
            System.out.println("[6] Add Study Space");
            System.out.println("[7] Logout");
            System.out.println("========================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    dao.displayBookings("ALL BOOKINGS");
                    pause();
                    break;

                case "2":
                    approveBookings(staff);
                    break;

                case "3":
                    manageBookings(staff);
                    break;

                case "4":
                    checkOutStudents(staff);
                    break;

                case "5":
                    markNoShow(staff);
                    break;

                case "6":
                    addStudySpace(staff);
                    break;

                case "7":
                    running = false;
                    System.out.println("Staff logged out successfully!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // =====================================
    // [2] APPROVE / REJECT
    // =====================================
    private void approveBookings(User staff) {

        int shown = dao.displayBookings("PENDING BOOKINGS", "PENDING");

        if (shown == 0) {
            pause();
            return;
        }

        int bookingId = readBookingId("Enter Booking ID (0 to go back): ");
        if (bookingId == 0) {
            return;
        }

        System.out.print("[A] Approve   [R] Reject   [Enter] Cancel: ");
        String decision = scanner.nextLine().trim();

        if (decision.equalsIgnoreCase("A")
                || decision.equalsIgnoreCase("APPROVE")) {

            if (dao.changeStatus(staff.user_id(), bookingId, "APPROVED",
                    "APPROVE_BOOKING", "PENDING")) {
                System.out.println("\nBooking #" + bookingId + " approved.");
            }

        } else if (decision.equalsIgnoreCase("R")
                || decision.equalsIgnoreCase("REJECT")) {

            if (dao.changeStatus(staff.user_id(), bookingId, "REJECTED",
                    "REJECT_BOOKING", "PENDING")) {
                System.out.println("\nBooking #" + bookingId + " rejected.");
            }

        } else {
            System.out.println("No change made.");
        }

        pause();
    }

    // =====================================
    // [3] MANAGE BOOKINGS
    // =====================================
    private void manageBookings(User staff) {

        boolean managing = true;

        while (managing) {

            System.out.println("\n================================");
            System.out.println("         MANAGE BOOKINGS");
            System.out.println("================================");
            System.out.println("[1] Cancel Booking");
            System.out.println("[2] Check-in Student");
            System.out.println("[3] Back to Staff Dashboard");
            System.out.println("================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    changeBooking(staff, "ACTIVE BOOKINGS", "CANCELLED",
                            "CANCEL_BOOKING", "cancel",
                            "PENDING", "APPROVED");
                    break;

                case "2":
                    changeBooking(staff, "APPROVED BOOKINGS", "CHECKED_IN",
                            "CHECK_IN", "check in",
                            "APPROVED");
                    break;

                case "3":
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // =====================================
    // [4] CHECK-OUT
    // =====================================
    private void checkOutStudents(User staff) {

        changeBooking(staff, "CHECKED-IN STUDENTS", "COMPLETED",
                "CHECK_OUT", "check out",
                "CHECKED_IN");
    }

    // =====================================
    // [5] NO-SHOW
    // =====================================
    private void markNoShow(User staff) {

        changeBooking(staff, "APPROVED BOOKINGS", "NO_SHOW",
                "NO_SHOW", "mark as no-show",
                "APPROVED");
    }

    // Shared flow: list -> pick ID -> confirm -> update.
    private void changeBooking(User staff, String heading, String newStatus,
                               String action, String verb,
                               String... allowedFrom) {

        int shown = dao.displayBookings(heading, allowedFrom);

        if (shown == 0) {
            pause();
            return;
        }

        int bookingId = readBookingId(
                "Enter Booking ID to " + verb + " (0 to go back): ");

        if (bookingId == 0) {
            return;
        }

        if (confirm("Confirm " + verb + " booking #" + bookingId + "? (Y/N): ")) {

            if (dao.changeStatus(staff.user_id(), bookingId, newStatus,
                    action, allowedFrom)) {
                System.out.println("\nDone. Booking #" + bookingId
                        + " is now " + newStatus + ".");
            }

        } else {
            System.out.println("Cancelled. No change made.");
        }

        pause();
    }

    // =====================================
    // [6] ADD STUDY SPACE  (saved to study_spaces)
    // =====================================
    private void addStudySpace(User staff) {

        System.out.println("\n========== ADD STUDY SPACE ==========");

        showStudySpaces();

        System.out.println("\nEnter the details of the new study space.");

        String name = readRequired("Space Name: ");
        String type = readSpaceType();
        int capacity = readPositiveInteger("Capacity (number of people): ");
        String location = readRequired("Location (e.g. Library - 1st Floor): ");
        boolean hasOutlet = confirm("Has power outlet? (Y/N): ");

        System.out.println("\n---------- NEW STUDY SPACE ----------");
        System.out.println("Name     : " + name);
        System.out.println("Type     : " + type);
        System.out.println("Capacity : " + capacity);
        System.out.println("Location : " + location);
        System.out.println("Outlet   : " + (hasOutlet ? "Yes" : "No"));
        System.out.println("Status   : AVAILABLE");

        if (!confirm("\nSave this study space? (Y/N): ")) {
            System.out.println("Cancelled. Nothing was saved.");
            pause();
            return;
        }

        String sql = "INSERT INTO study_spaces "
                + "(space_name, space_type, capacity, location, has_outlet, status) "
                + "VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setInt(3, capacity);
            ps.setString(4, location);
            ps.setInt(5, hasOutlet ? 1 : 0);

            if (ps.executeUpdate() > 0) {

                int newId = 0;

                try (PreparedStatement idPs =
                             conn.prepareStatement("SELECT last_insert_rowid()");
                     ResultSet rs = idPs.executeQuery()) {

                    if (rs.next()) {
                        newId = rs.getInt(1);
                    }
                }

                System.out.println("\nStudy space added successfully! Space ID: "
                        + newId);

                try {
                    new ActivityLogDao(conn).logAction(
                            staff.user_id(), "ADD_SPACE",
                            "Added study space #" + newId + " " + name);
                } catch (Exception logError) {
                    System.out.println("(Activity log not saved: "
                            + logError.getMessage() + ")");
                }

            } else {
                System.out.println("\nStudy space was not added.");
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }

        showStudySpaces();
        pause();
    }

    private void showStudySpaces() {

        String sql = "SELECT space_id, space_name, space_type, capacity, "
                + "location, has_outlet, status "
                + "FROM study_spaces ORDER BY space_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n---------------------- STUDY SPACES ----------------------");
            System.out.printf("%-4s %-20s %-17s %-5s %-22s %-7s %-12s%n",
                    "ID", "Name", "Type", "Cap.", "Location", "Outlet", "Status");
            System.out.println("-".repeat(94));

            boolean any = false;

            while (rs.next()) {
                any = true;
                System.out.printf("%-4d %-20s %-17s %-5d %-22s %-7s %-12s%n",
                        rs.getInt("space_id"),
                        rs.getString("space_name"),
                        rs.getString("space_type"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getInt("has_outlet") == 1 ? "Yes" : "No",
                        rs.getString("status"));
            }

            if (!any) {
                System.out.println("No study spaces found.");
            }

        } catch (SQLException e) {
            System.out.println("Error loading study spaces: " + e.getMessage());
        }
    }

    private String readSpaceType() {

        while (true) {

            System.out.println("Space Type:");
            System.out.println("  [1] PRIVATE ROOM");
            System.out.println("  [2] DISCUSSION ROOM");
            System.out.println("  [3] COMPUTER SPACE");
            System.out.println("  [4] Other (type your own)");
            System.out.print("Choose type: ");

            switch (scanner.nextLine().trim()) {

                case "1":
                    return "PRIVATE ROOM";

                case "2":
                    return "DISCUSSION ROOM";

                case "3":
                    return "COMPUTER SPACE";

                case "4":
                    return readRequired("Enter space type: ").toUpperCase();

                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
            }
        }
    }

    // =====================================
    // HELPERS
    // =====================================

    // Accepts: Y, y, YES, yes ...   Anything else counts as No.
    private boolean confirm(String message) {

        System.out.print(message);

        String answer = scanner.nextLine().trim();

        return answer.equalsIgnoreCase("Y")
                || answer.equalsIgnoreCase("YES");
    }

    private String readRequired(String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field is required.");
        }
    }

    private int readPositiveInteger(String message) {

        while (true) {

            System.out.print(message);

            try {
                int n = Integer.parseInt(scanner.nextLine().trim());

                if (n > 0) {
                    return n;
                }

                System.out.println("Please enter a number greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Returns 0 to go back, otherwise a positive booking ID.
    private int readBookingId(String message) {

        while (true) {

            System.out.print(message);

            try {
                int id = Integer.parseInt(scanner.nextLine().trim());

                if (id >= 0) {
                    return id;
                }

                System.out.println("Please enter 0 or a positive number.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void pause() {

        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}