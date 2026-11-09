
package com.scc.ui;

import com.scc.dao.StaffBookingDao;
import com.scc.model.User;

import java.util.Scanner;

public class StaffMenu {

    private final Scanner scanner;
    private final StaffBookingDao dao;

    public StaffMenu(Scanner scanner) {
        this.scanner = scanner;
        this.dao = new StaffBookingDao();
    }

    // =====================================
    // MAIN STAFF DASHBOARD
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
            System.out.println("[6] Logout");

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
                    running = false;
                    System.out.println("Staff logged out successfully!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    pause();
                    break;
            }
        }
    }

    // =====================================
    // [2] APPROVE / REJECT BOOKINGS
    // =====================================
    private void approveBookings(User staff) {

        int shown = dao.displayBookings("PENDING BOOKINGS", "PENDING");

        if (shown == 0) {
            pause();
            return;
        }

        int bookingId = readBookingId(
                "Enter Booking ID (0 to go back): ");

        if (bookingId == 0) {
            return;
        }

        System.out.print(
                "[A] Approve   [R] Reject   [Enter] Cancel: ");

        String decision = scanner.nextLine().trim();

        if (decision.equalsIgnoreCase("A")
                || decision.equalsIgnoreCase("APPROVE")) {

            if (dao.changeStatus(
                    staff.user_id(),
                    bookingId,
                    "APPROVED",
                    "APPROVE_BOOKING",
                    "PENDING")) {

                System.out.println(
                        "\nBooking #" + bookingId + " approved.");
            }

        } else if (decision.equalsIgnoreCase("R")
                || decision.equalsIgnoreCase("REJECT")) {

            if (dao.changeStatus(
                    staff.user_id(),
                    bookingId,
                    "REJECTED",
                    "REJECT_BOOKING",
                    "PENDING")) {

                System.out.println(
                        "\nBooking #" + bookingId + " rejected.");
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
                    changeBooking(
                            staff,
                            "ACTIVE BOOKINGS",
                            "CANCELLED",
                            "CANCEL_BOOKING",
                            "cancel",
                            "PENDING",
                            "APPROVED"
                    );
                    break;

                case "2":
                    changeBooking(
                            staff,
                            "APPROVED BOOKINGS",
                            "CHECKED_IN",
                            "CHECK_IN",
                            "check in",
                            "APPROVED"
                    );
                    break;

                case "3":
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    pause();
                    break;
            }
        }
    }

    // =====================================
    // [4] CHECK-OUT STUDENTS
    // =====================================
    private void checkOutStudents(User staff) {

        changeBooking(
                staff,
                "CHECKED-IN STUDENTS",
                "COMPLETED",
                "CHECK_OUT",
                "check out",
                "CHECKED_IN"
        );
    }

    // =====================================
    // [5] MARK NO-SHOW
    // =====================================
    private void markNoShow(User staff) {

        changeBooking(
                staff,
                "APPROVED BOOKINGS",
                "NO_SHOW",
                "NO_SHOW",
                "mark as no-show",
                "APPROVED"
        );
    }

    // =====================================
    // SHARED BOOKING UPDATE FUNCTION
    // =====================================
    private void changeBooking(
            User staff,
            String heading,
            String newStatus,
            String action,
            String verb,
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

        if (confirm(
                "Confirm " + verb + " booking #" + bookingId + "? (Y/N): ")) {

            if (dao.changeStatus(
                    staff.user_id(),
                    bookingId,
                    newStatus,
                    action,
                    allowedFrom)) {

                System.out.println(
                        "\nDone. Booking #" + bookingId
                                + " is now " + newStatus + ".");
            }

        } else {
            System.out.println("Cancelled. No change made.");
        }

        pause();
    }

    // =====================================
    // CONFIRMATION HELPER
    // =====================================
    private boolean confirm(String message) {

        System.out.print(message);

        String answer = scanner.nextLine().trim();

        return answer.equalsIgnoreCase("Y")
                || answer.equalsIgnoreCase("YES");
    }

    // =====================================
    // BOOKING ID INPUT
    // =====================================
    private int readBookingId(String message) {

        while (true) {

            System.out.print(message);

            try {

                int id = Integer.parseInt(
                        scanner.nextLine().trim());

                if (id >= 0) {
                    return id;
                }

                System.out.println(
                        "Please enter 0 or a positive number.");

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input. Please enter a number.");
            }
        }
    }

    // =====================================
    // PAUSE
    // =====================================
    private void pause() {

        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
