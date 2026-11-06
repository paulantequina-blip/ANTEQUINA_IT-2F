package com.scc.ui;

import com.scc.dao.BookingDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class StudentMenu {

    private final Scanner scanner;
    private final BookingDao bookingDao;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public StudentMenu(Scanner scanner) {
        this.scanner = scanner;
        this.bookingDao = new BookingDao();
    }

    public void display(User student) {

        boolean running = true;

        while (running) {

            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║       STUDY SPACE BOOKING SYSTEM       ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║             STUDENT MENU               ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println(" Welcome, " + student.fullName());
            System.out.println("──────────────────────────────────────────");
            System.out.println(" [1] View Study Spaces");
            System.out.println(" [2] Check Space Availability");
            System.out.println(" [3] Create Booking");
            System.out.println(" [4] Manage My Bookings");
            System.out.println(" [5] View My Booking History");
            System.out.println(" [6] Logout");
            System.out.println("══════════════════════════════════════════");
            System.out.print(" Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    bookingDao.displayStudySpaces();
                    pause();
                    break;

                case "2":
                    // View only. Does not create or change bookings.
                    bookingDao.displaySpaceAvailability();
                    pause();
                    break;

                case "3":
                    createBooking(student);
                    break;

                case "4":
                    manageMyBookings(student);
                    break;

                case "5":
                    bookingDao.displayMyBookings(student.user_id());
                    pause();
                    break;

                case "6":
                    System.out.println("\n╔════════════════════════════════════════╗");
                    System.out.println("║           LOGGING OUT...               ║");
                    System.out.println("╚════════════════════════════════════════╝");
                    running = false;
                    break;

                default:
                    System.out.println(" [!] Invalid choice. Please try again.");
                    pause();
            }
        }
    }

    // OPTION 3: CREATE BOOKING
    private void createBooking(User student) {

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            CREATE BOOKING              ║");
        System.out.println("╚════════════════════════════════════════╝");

        bookingDao.displayStudySpaces();

        int spaceId = readPositiveInteger("\n Enter Study Space ID: ");

        LocalDate bookingDate = readBookingDate();

        LocalTime startTime = readTime(" Enter Start Time (HH:MM): ");

        LocalTime endTime;

        while (true) {

            endTime = readTime(" Enter End Time (HH:MM): ");

            if (endTime.isAfter(startTime)) {
                break;
            }

            System.out.println(" [!] Invalid time! End time must be later than start time.");
        }

        String date = bookingDate.toString();
        String start = startTime.format(TIME_FORMAT);
        String end = endTime.format(TIME_FORMAT);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            BOOKING DETAILS             ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println(" Student Name   : " + student.fullName());
        System.out.println(" Student ID     : " + student.user_id());
        System.out.println(" Study Space ID : " + spaceId);
        System.out.println(" Booking Date   : " + date);
        System.out.println(" Start Time     : " + start);
        System.out.println(" End Time       : " + end);
        System.out.println(" Status         : PENDING");
        System.out.println("──────────────────────────────────────────");

        try {

            boolean available = bookingDao.isSpaceAvailable(
                    spaceId, date, start, end, 0
            );

            if (!available) {
                System.out.println("\n [X] Booking cannot be created. Space is occupied.");
                pause();
                return;
            }

            System.out.println("\n [✔] The selected time is available.");

            if (confirm(" Confirm booking? (Y/N): ")) {

                boolean saved = bookingDao.createBooking(
                        student.user_id(),
                        spaceId,
                        date,
                        start,
                        end
                );

                if (saved) {
                    System.out.println("\n╔════════════════════════════════════════╗");
                    System.out.println("║      BOOKING SAVED SUCCESSFULLY!       ║");
                    System.out.println("║      Status: PENDING                   ║");
                    System.out.println("╚════════════════════════════════════════╝");
                } else {
                    System.out.println(" [X] Booking was not saved.");
                }

            } else {
                System.out.println(" [!] Booking cancelled by student.");
            }

        } catch (SQLException e) {
            System.out.println(" [X] Booking error: " + e.getMessage());
        }

        pause();
    }

    // READ AND VALIDATE DATE
    private LocalDate readBookingDate() {

        while (true) {

            System.out.print(" Enter Booking Date (YYYY-MM-DD): ");

            String input = scanner.nextLine().trim();

            try {

                LocalDate date = LocalDate.parse(input);

                if (date.isBefore(LocalDate.now())) {
                    System.out.println(" [!] You cannot book a date in the past.");
                    continue;
                }

                return date;

            } catch (DateTimeParseException e) {
                System.out.println(" [!] Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    // READ AND VALIDATE TIME
    private LocalTime readTime(String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            try {

                return LocalTime.parse(input, TIME_FORMAT);

            } catch (DateTimeParseException e) {
                System.out.println(" [!] Invalid time format. Please use HH:MM.");
            }
        }
    }

    // READ POSITIVE INTEGER
    private int readPositiveInteger(String message) {

        while (true) {

            System.out.print(message);

            try {

                int number = Integer.parseInt(
                        scanner.nextLine().trim()
                );

                if (number > 0) {
                    return number;
                }

                System.out.println(" [!] Please enter a number greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println(" [!] Invalid input. Please enter a number.");
            }
        }
    }

    // YES / NO QUESTION
    // Accepts: Y, y, YES, yes, Yes ...   Anything else counts as No.
    private boolean confirm(String message) {

        System.out.print(message);

        String answer = scanner.nextLine().trim();

        return answer.equalsIgnoreCase("Y")
                || answer.equalsIgnoreCase("YES");
    }

    // OPTION 4: MANAGE MY BOOKINGS
    private void manageMyBookings(User student) {

        boolean managing = true;

        while (managing) {

            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║          MANAGE MY BOOKINGS            ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println(" [1] Edit My Upcoming Booking");
            System.out.println(" [2] Cancel My Upcoming Booking");
            System.out.println(" [3] Back to Student Menu");
            System.out.println("══════════════════════════════════════════");
            System.out.print(" Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    editBooking(student);
                    break;

                case "2":
                    cancelBooking(student);
                    break;

                case "3":
                    managing = false;
                    break;

                default:
                    System.out.println(" [!] Invalid choice.");
                    pause();
            }
        }
    }

    // EDIT BOOKING
    private void editBooking(User student) {

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            EDIT MY BOOKING             ║");
        System.out.println("╚════════════════════════════════════════╝");

        bookingDao.displayMyUpcomingBookings(student.user_id());

        int bookingId = readPositiveInteger(
                "\n Enter Booking ID to edit: "
        );

        LocalDate bookingDate = readBookingDate();

        LocalTime startTime = readTime(
                " Enter New Start Time (HH:MM): "
        );

        LocalTime endTime;

        while (true) {

            endTime = readTime(" Enter New End Time (HH:MM): ");

            if (endTime.isAfter(startTime)) {
                break;
            }

            System.out.println(" [!] End time must be later than start time.");
        }

        if (confirm(" Confirm changes? (Y/N): ")) {

            boolean updated = bookingDao.updateBooking(
                    student.user_id(),
                    bookingId,
                    bookingDate.toString(),
                    startTime.format(TIME_FORMAT),
                    endTime.format(TIME_FORMAT)
            );

            if (updated) {
                System.out.println(" [✔] Booking updated successfully!");
            } else {
                System.out.println(" [X] Booking was not updated.");
            }

        } else {
            System.out.println(" [!] Update cancelled.");
        }

        pause();
    }

    // CANCEL BOOKING
    private void cancelBooking(User student) {

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           CANCEL MY BOOKING            ║");
        System.out.println("╚════════════════════════════════════════╝");

        bookingDao.displayMyUpcomingBookings(student.user_id());

        int bookingId = readPositiveInteger(
                "\n Enter Booking ID to cancel: "
        );

        if (confirm(" Are you sure you want to cancel? (Y/N): ")) {

            bookingDao.cancelBooking(
                    student.user_id(),
                    bookingId
            );

        } else {
            System.out.println(" [!] Cancellation aborted.");
        }

        pause();
    }

    // PAUSE
    private void pause() {

        System.out.println("\n Press ENTER to continue...");
        scanner.nextLine();
    }
}