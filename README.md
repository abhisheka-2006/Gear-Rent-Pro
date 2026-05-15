# GearRent Pro

GearRent Pro is a JavaFX desktop application for managing an equipment rental business. It includes login and role-based dashboard access, branch and equipment management, customer records, reservations, rentals, returns, overdue tracking, and reporting.

## Features

- Secure login screen with role-based access control
- Dashboard navigation for core rental workflows
- Branch management for multi-location operations
- Equipment, category, and availability tracking
- Customer management with membership levels
- Reservation and rental processing
- Return handling with late fees and damage charges
- Overdue rental view and reporting screens

## Tech Stack

- Java 17
- JavaFX 21
- Maven
- H2 embedded database by default
- MySQL JDBC driver included for MySQL compatibility

## Project Structure

- `src/main/java/com/gearrent` - application code
- `src/main/resources/ui` - JavaFX FXML views and CSS
- `sql/schema.sql` - database schema
- `sql/sample_data.sql` - seed data
- `data/` - local H2 database files

## Prerequisites

- JDK 17
- Maven 3.8+ or 3.9+

## Run

From the project root, run:

```bash
mvn javafx:run
```

If you are launching from outside the project directory, use the explicit POM path:

```bash
mvn -f "d:\CMJD\CW2\GearRentPro_clean\GearRentPro\pom.xml" javafx:run
```

## Database

The application uses H2 by default with the file-backed database at `./data/gearrentpro`.

On first run, the app initializes the schema and sample data automatically from:

- `sql/schema.sql`
- `sql/sample_data.sql`

## Default Login Accounts

The application seeds these sample accounts:

- `admin` / `admin123`
- `manager1` / `mgr123`
- `manager2` / `mgr123`
- `staff1` / `staff123`
- `staff2` / `staff123`

## Notes

- The login screen opens first, and successful authentication loads the dashboard.
- Admin-only branch actions are hidden for non-admin users.
- If the H2 database already exists, the app reuses it instead of recreating it.
