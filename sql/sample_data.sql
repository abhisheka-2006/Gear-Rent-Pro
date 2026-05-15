USE gearrentpro;
 
 -- ── BRANCHES (3 required) ─────────────────────────────────────────────
 INSERT INTO branch (branch_code, name, address, contact) VALUES
 ('PAN','Panadura Branch','123 Galle Road, Panadura','0112 250 100'),
 ('GAL','Galle Branch','45 Matara Road, Galle','0912 234 500'),
 ('COL','Colombo Branch','78 Duplication Rd, Colombo 3','0112 678 900');
 
 -- ── SYSTEM USERS ──────────────────────────────────────────────────────
 INSERT INTO `system_user` (username,password,full_name,role,branch_id) VALUES
 ('admin',   'admin123', 'System Administrator', 'ADMIN',          NULL),
 ('manager1','mgr123',   'Panadura Manager',     'BRANCH_MANAGER', 1),
 ('manager2','mgr123',   'Galle Manager',        'BRANCH_MANAGER', 2),
 ('staff1',  'staff123', 'Panadura Staff',       'STAFF',          1),
 ('staff2',  'staff123', 'Galle Staff',          'STAFF',          2);
 
 -- ── EQUIPMENT CATEGORIES (5 required) ────────────────────────────────
 INSERT INTO equipment_category
   (name, description, base_price_factor, weekend_multiplier, late_fee_per_day)
 VALUES
 ('Camera',    'Professional cameras',          1.0, 1.2, 500),
 ('Lens',      'Camera lenses & adapters',       0.8, 1.1, 300),
 ('Drone',     'Aerial photography drones',      2.0, 1.3, 800),
 ('Lighting',  'Studio & outdoor lighting kits', 0.9, 1.1, 400),
 ('Audio',     'Microphones & audio recorders',  1.1, 1.2, 350);
 
 -- ── EQUIPMENT (20+ items across branches) ────────────────────────────
 -- Branch 1 - Panadura (branch_id=1)
 INSERT INTO equipment
   (equipment_code,category_id,branch_id,brand,model,
    purchase_year,daily_base_price,security_deposit,status)
 VALUES
 ('EQ001',1,1,'Sony','Alpha A7 IV',2022,3500,25000,'AVAILABLE'),
 ('EQ002',1,1,'Canon','EOS R5',    2021,4000,30000,'AVAILABLE'),
 ('EQ003',2,1,'Sony','FE 24-70mm', 2021,1500,15000,'AVAILABLE'),
 ('EQ004',2,1,'Canon','RF 50mm f1.2',2022,1200,12000,'AVAILABLE'),
 ('EQ005',3,1,'DJI', 'Mavic 3 Pro',2023,6000,50000,'AVAILABLE'),
 ('EQ006',4,1,'Godox','SL150W LED',2020, 800, 8000,'AVAILABLE'),
 ('EQ007',5,1,'Rode','VideoMic Pro',2021, 700, 6000,'AVAILABLE'),
 ('EQ008',1,1,'Nikon','Z6 II',     2022,3200,22000,'RENTED'),
 -- Branch 2 - Galle (branch_id=2)
 ('EQ009',1,2,'Sony','Alpha A7 III',2020,2800,20000,'AVAILABLE'),
 ('EQ010',1,2,'Fujifilm','X-T5',   2023,2500,18000,'AVAILABLE'),
 ('EQ011',2,2,'Sigma','35mm Art',   2019,1000,10000,'AVAILABLE'),
 ('EQ012',3,2,'DJI','Air 2S',       2021,4000,35000,'AVAILABLE'),
 ('EQ013',3,2,'DJI','Inspire 2',    2020,8000,70000,'UNDER_MAINTENANCE'),
 ('EQ014',4,2,'Aputure','300d Mark II',2022,1200,12000,'AVAILABLE'),
 ('EQ015',5,2,'Zoom','H6 Recorder', 2020, 600, 5000,'AVAILABLE'),
 -- Branch 3 - Colombo (branch_id=3)
 ('EQ016',1,3,'Canon','EOS R3',     2022,5000,40000,'AVAILABLE'),
 ('EQ017',1,3,'Sony','Alpha A1',    2021,6000,50000,'AVAILABLE'),
 ('EQ018',2,3,'Zeiss','Otus 55mm',  2020,2000,20000,'RESERVED'),
 ('EQ019',3,3,'DJI','Matrice 300',  2022,12000,100000,'AVAILABLE'),
 ('EQ020',4,3,'Godox','SK400II',    2021, 900, 9000,'AVAILABLE'),
 ('EQ021',5,3,'Sennheiser','MKH416',2020,1100,10000,'AVAILABLE');
 
 -- ── CUSTOMERS (10+ with different membership levels) ─────────────────
 INSERT INTO customer (name,nic_passport,contact_no,email,address,membership_level)
 VALUES
 ('Kasun Perera',    '199012345678','0771234567','kasun@email.com',   'Panadura',  'GOLD'),
 ('Nimali Fernando', '198567890123','0782345678','nimali@email.com',  'Galle',     'SILVER'),
 ('Roshan Silva',    '200001234567','0713456789','roshan@email.com',  'Colombo 7', 'REGULAR'),
 ('Sanduni Jayawardena','199534567890','0764567890','sanduni@email.com','Panadura','GOLD'),
 ('Chamara Wickrama', '198912345678','0775678901','chamara@email.com', 'Matara',   'SILVER'),
 ('Dilini Rathnayake','200112345678','0776789012','dilini@email.com',  'Colombo 3','REGULAR'),
 ('Samantha Gunawardena','199878901234','0787890123','sam@email.com',  'Galle',    'GOLD'),
 ('Tharaka Bandara', '199789012345','0778901234','tharaka@email.com',  'Kandy',    'REGULAR'),
 ('Priyanka De Silva','199890123456','0779012345','priyanka@email.com','Colombo 5','SILVER'),
 ('Mahesh Kumara',   '200034567890','0770123456','mahesh@email.com',  'Panadura', 'REGULAR');
 
 -- ── SAMPLE RENTALS (including overdue and returned) ──────────────────
 -- Rental 1: Active - Kasun rented EQ001 (Sony A7 IV)
 INSERT INTO rental
   (equipment_id,customer_id,branch_id,start_date,end_date,
    rental_amount,security_deposit,long_rental_discount,membership_discount,
    final_payable,payment_status,rental_status)
 VALUES
 (1,1,1,'2025-01-05','2025-01-09', 17500,25000,0,1750,40750,'PAID','ACTIVE'),
 -- Rental 2: OVERDUE - Roshan rented EQ009 (far past end date)
 (9,3,2,'2024-12-01','2024-12-05', 11200,20000,0,0,31200,'PAID','ACTIVE'),
 -- Rental 3: RETURNED with damage - Nimali
 (6,2,1,'2025-01-01','2025-01-03', 1600,8000,0,80,9520,
  'PAID','RETURNED'),
 -- Rental 4: Returned on time - Sanduni (GOLD member, long rental)
 (5,4,1,'2024-12-10','2024-12-20', 60000,50000,6000,5400,109400,
  'PAID','RETURNED');
 
 -- Update actual return for returned rentals
 UPDATE rental SET actual_return_date='2025-01-03',
   damage_charge=5000, damage_description='Cracked diffuser panel'
   WHERE rental_id=3;
 UPDATE rental SET actual_return_date='2024-12-20' WHERE rental_id=4;
 
 -- Update equipment statuses to match rentals
 UPDATE equipment SET status='RENTED'         WHERE equipment_id=1;  -- EQ001
 UPDATE equipment SET status='RENTED'         WHERE equipment_id=9;  -- EQ009
 UPDATE equipment SET status='UNDER_MAINTENANCE' WHERE equipment_id=6; -- EQ006 damaged
 
 -- ── RESERVATION ──────────────────────────────────────────────────────
 INSERT INTO reservation (equipment_id,customer_id,branch_id,start_date,end_date,status)
 VALUES (18, 7, 3, '2025-01-15', '2025-01-18', 'ACTIVE');
