CREATE DATABASE IF NOT EXISTS gearrentpro;
 USE gearrentpro;
 
 -- ───────────────────────────────────────
 -- 1. BRANCHES
 -- ───────────────────────────────────────
 CREATE TABLE branch (
   branch_id   INT AUTO_INCREMENT PRIMARY KEY,
   branch_code VARCHAR(10)  NOT NULL UNIQUE,
   name        VARCHAR(100) NOT NULL,
   address     VARCHAR(255),
   contact     VARCHAR(20)
 );
 
 -- ───────────────────────────────────────
 -- 2. EQUIPMENT CATEGORIES
 -- ───────────────────────────────────────
 CREATE TABLE equipment_category (
   category_id        INT AUTO_INCREMENT PRIMARY KEY,
   name               VARCHAR(50) NOT NULL,
   description        TEXT,
   base_price_factor  DOUBLE  NOT NULL DEFAULT 1.0,
   weekend_multiplier DOUBLE  NOT NULL DEFAULT 1.0,
   late_fee_per_day   DOUBLE  NOT NULL DEFAULT 500.0,
   is_active          TINYINT NOT NULL DEFAULT 1
 );
 
 -- ───────────────────────────────────────
 -- 3. EQUIPMENT
 -- ───────────────────────────────────────
 CREATE TABLE equipment (
   equipment_id    INT AUTO_INCREMENT PRIMARY KEY,
   equipment_code  VARCHAR(20) NOT NULL UNIQUE,
   category_id     INT NOT NULL,
   branch_id       INT NOT NULL,
   brand           VARCHAR(50),
   model           VARCHAR(100),
   purchase_year   INT,
   daily_base_price DOUBLE NOT NULL,
   security_deposit DOUBLE NOT NULL,
   status          ENUM('AVAILABLE','RESERVED','RENTED','UNDER_MAINTENANCE')
                   NOT NULL DEFAULT 'AVAILABLE',
   FOREIGN KEY (category_id) REFERENCES equipment_category(category_id),
   FOREIGN KEY (branch_id)   REFERENCES branch(branch_id)
 );
 
 -- ───────────────────────────────────────
 -- 4. CUSTOMERS
 -- ───────────────────────────────────────
 CREATE TABLE customer (
   customer_id      INT AUTO_INCREMENT PRIMARY KEY,
   name             VARCHAR(100) NOT NULL,
   nic_passport     VARCHAR(30)  NOT NULL UNIQUE,
   contact_no       VARCHAR(20),
   email            VARCHAR(100),
   address          TEXT,
   membership_level ENUM('REGULAR','SILVER','GOLD') NOT NULL DEFAULT 'REGULAR'
 );
 
 -- ───────────────────────────────────────
 -- 5. MEMBERSHIP CONFIG
 -- ───────────────────────────────────────
 CREATE TABLE membership_config (
   level            ENUM('REGULAR','SILVER','GOLD') PRIMARY KEY,
   discount_percent DOUBLE NOT NULL DEFAULT 0
 );
 INSERT INTO membership_config VALUES ('REGULAR',0),('SILVER',5),('GOLD',10);
 
 -- ───────────────────────────────────────
 CREATE TABLE `system_user` (
   user_id   INT AUTO_INCREMENT PRIMARY KEY,
   username  VARCHAR(50) NOT NULL UNIQUE,
   password  VARCHAR(100) NOT NULL,
   full_name VARCHAR(100),
   role      ENUM('ADMIN','BRANCH_MANAGER','STAFF') NOT NULL,
   branch_id INT,
   FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
 );
 
 -- ───────────────────────────────────────
 -- 7. RESERVATIONS
 -- ───────────────────────────────────────
 CREATE TABLE reservation (
   reservation_id INT AUTO_INCREMENT PRIMARY KEY,
   equipment_id   INT NOT NULL,
   customer_id    INT NOT NULL,
   branch_id      INT NOT NULL,
   start_date     DATE NOT NULL,
   end_date       DATE NOT NULL,
   status         ENUM('ACTIVE','CONVERTED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
   created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
   FOREIGN KEY (customer_id)  REFERENCES customer(customer_id),
   FOREIGN KEY (branch_id)    REFERENCES branch(branch_id)
 );
 
 -- ───────────────────────────────────────
 -- 8. RENTALS
 -- ───────────────────────────────────────
 CREATE TABLE rental (
   rental_id             INT AUTO_INCREMENT PRIMARY KEY,
   equipment_id          INT NOT NULL,
   customer_id           INT NOT NULL,
   branch_id             INT NOT NULL,
   reservation_id        INT,
   start_date            DATE NOT NULL,
   end_date              DATE NOT NULL,
   actual_return_date    DATE,
   rental_amount         DOUBLE NOT NULL,
   security_deposit      DOUBLE NOT NULL,
   long_rental_discount  DOUBLE NOT NULL DEFAULT 0,
   membership_discount   DOUBLE NOT NULL DEFAULT 0,
   final_payable         DOUBLE NOT NULL,
   late_fee              DOUBLE NOT NULL DEFAULT 0,
   damage_charge         DOUBLE NOT NULL DEFAULT 0,
   damage_description    TEXT,
   payment_status        ENUM('PAID','PARTIALLY_PAID','UNPAID') NOT NULL DEFAULT 'UNPAID',
   rental_status         ENUM('ACTIVE','RETURNED','OVERDUE','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
   created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (equipment_id)   REFERENCES equipment(equipment_id),
   FOREIGN KEY (customer_id)    REFERENCES customer(customer_id),
   FOREIGN KEY (branch_id)      REFERENCES branch(branch_id),
   FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
 );
