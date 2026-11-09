
package com.scc.ui;

import com.scc.dao.BookingDao;
import com.scc.model.User;

import java.util.Scanner;

public class StudentMenu {

    private final Scanner scanner;
    private final BookingDao bookingDao;

    private static final String BORDER =
            "+------------------------------------------------------------------------------+";

    public StudentMenu(Scanner scanner) {
        this.scanner = scanner;
        this.bookingDao = new BookingDao();
    }

    // ==========================================
    //             STUDENT DASHBOARD
    // ==========================================
    public void display(User student) {

        boolean running = true;

        while (running) {

            System.out.println("\n" + BORDER);
            printLine("STUDY SPACE BOOKING SYSTEM");
            System.out.println(BORDER);

            printLine("STUDENT MENU");
            System.out.println(BORDER);

            printLine("Welcome, " + student.fullName());
            System.out.println(BORDER);

            printLine("[1] Check Space Availability");
            printLine("[2] Create Booking");
            printLine("[3] Manage My Bookings");
            printLine("[4] View My Booking History");
            printLine("[5] Logout");

            System.out.println(BORDER);
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    // VIEW ONLY
                    bookingDao.displaySpaceAvailability();
                    pause();
                    break;

                case "2":
                    createBooking(student);
                    break;

                case "3":
                    manageMyBookings(student);
                    break;

                case "4":
                    bookingDao.displayMyBookings(student.user_id());
                    pause();
                    break;

                case "5":
                    System.out.println("\n" + BORDER);
                    printLine("STUDENT LOGGED OUT SUCCESSFULLY");
                    System.out.println(BORDER);
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    pause();
                    break;
            }
        }
    }

    // ==========================================
    //              CREATE BOOKING
    // ==========================================
    private void createBooking(User student) {

        System.out.println("\n" + BORDER);
        printLine("CREATE BOOKING");
        System.out.println(BORDER);

        // Display available study spaces
        bookingDao.displayStudySpaces();

        System.out.print("\nEnter Space ID: ");

        int spaceId;

        try {
            spaceId = Integer.parseInt(scanner.nextLine().trim());

            if (spaceId <= 0) {
                System.out.println("Invalid Space ID.");
                pause();
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid Space ID.");
            pause();
            return;
        }

        System.out.print("Enter Booking Date (YYYY-MM-DD): ");
        String bookingDate = scanner.nextLine().trim();

        System.out.print("Enter Start Time (HH:MM): ");
        String startTime = scanner.nextLine().trim();

        System.out.print("Enter End Time (HH:MM): ");
        String endTime = scanner.nextLine().trim();

        // ======================================
        // BOOKING DETAILS
        // ======================================

        System.out.println("\n" + BORDER);
        printLine("BOOKING DETAILS");
        System.out.println(BORDER);

        printDetail("Student", student.fullName());
        printDetail("Space ID", String.valueOf(spaceId));
        printDetail("Booking Date", bookingDate);
        printDetail("Start Time", startTime);
        printDetail("End Time", endTime);

        System.out.println(BORDER);

        System.out.print("Confirm booking? (Y/N): ");

        String answer = scanner.nextLine().trim();

        // Accept Y or YES
        if (answer.equalsIgnoreCase("Y")
                || answer.equalsIgnoreCase("YES")) {

            boolean success = bookingDao.createBooking(
                    student.user_id(),
                    spaceId,
                    bookingDate,
                    startTime,
                    endTime
            );

            if (success) {
                System.out.println("\nBooking request submitted successfully!");
                System.out.println("Booking Status: PENDING");
            } else {
                System.out.println("\nBooking was not created.");
                System.out.println("Please check the space and time availability.");
            }

        } else {
            System.out.println("Booking cancelled.");
        }

        pause();
    }

    // ==========================================
    // MANAGE MY BOOKINGS
    // ==========================================
    private void manageMyBookings(User student) {

        boolean managing = true;

        while (managing) {

            System.out.println("\n" + BORDER);
            printLine("MANAGE MY BOOKINGS");
            System.out.println(BORDER);

            printLine("[1] View My Bookings");
            printLine("[2] Cancel My Booking");
            printLine("[3] Back");

            System.out.println(BORDER);
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    bookingDao.displayMyBookings(student.user_id());
                    pause();
                    break;

                case "2":
                    cancelMyBooking(student);
                    break;

                case "3":
                    managing = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
                    pause();
                    break;
            }
        }
    }

    // ==========================================
    // CANCEL MY BOOKING
    // ==========================================
    private void cancelMyBooking(User student) {

        System.out.println("\n" + BORDER);
        printLine("CANCEL MY BOOKING");
        System.out.println(BORDER);

        bookingDao.displayMyBookings(student.user_id());

        System.out.print("\nEnter Booking ID to cancel (0 to go back): ");

        try {

            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            if (bookingId == 0) {
                return;
            }

            if (bookingId < 0) {
                System.out.println("Invalid Booking ID.");
                pause();
                return;
            }

            System.out.print("Are you sure you want to cancel? (Y/N): ");

            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("Y")
                    || answer.equalsIgnoreCase("YES")) {

                // IMPORTANT:
                // First parameter = userId
                // Second parameter = bookingId

                boolean cancelled = bookingDao.cancelBooking(
                        student.user_id(),
                        bookingId
                );

                if (cancelled) {
                    System.out.println("Booking cancelled successfully!");
                } else {
                    System.out.println("Unable to cancel booking.");
                }

            } else {
                System.out.println("Cancellation cancelled.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Booking ID.");
        }

        pause();
    }

    // ==========================================
    // TABLE FORMATTING
    // ==========================================
    private void printLine(String text) {

        if (text == null) {
            text = "";
        }

        if (text.length() > 76) {
            text = text.substring(0, 73) + "...";
        }

        System.out.printf("| %-76s |%n", text);
    }

    private void printDetail(String label, String value) {

        if (value == null) {
            value = "";
        }

        if (value.length() > 53) {
            value = value.substring(0, 50) + "...";
        }

        System.out.printf("| %-20s | %-53s |%n", label, value);
    }

    // ==========================================
    // PAUSE
    // ==========================================
    private void pause() {

        System.out.println("\n" + BORDER);
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }
}
