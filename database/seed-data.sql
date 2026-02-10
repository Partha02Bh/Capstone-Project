USE banking_system_db;

-- 4. INSERT MOCK DATA (Admin & Standard Users)
-- Admin
INSERT INTO users (username, password, full_name, email, role) 
VALUES ('admin', 'admin123', 'Bank Manager', 'admin@bank.com', 'ROLE_ADMIN');

-- User 1: Partha
INSERT INTO users (username, password, full_name, email, role) 
VALUES ('Partha', 'Partha123', 'Partha', 'partha@gmail.com', 'ROLE_USER');

-- User 2: Prakhar
INSERT INTO users (username, password, full_name, email, role) 
VALUES ('Prakhar', 'Prakhar123', 'Prakhar', 'prakhar@gmail.com', 'ROLE_USER');

-- User 3: Nidhi
INSERT INTO users (username, password, full_name, email, role) 
VALUES ('Nidhi', 'Nidhi123', 'Nidhi', 'nidhi@gmail.com', 'ROLE_USER');

-- User 4: Krishna
INSERT INTO users (username, password, full_name, email, role) 
VALUES ('Krishna', 'Krishna123', 'Krishna', 'krishna@gmail.com', 'ROLE_USER');


-- Accounts (Using 10-digit standard numbers)
-- Partha's Account (User ID 2)
INSERT INTO accounts (user_id, account_number, balance, account_type) 
VALUES (2, '1000000001', 500000.00, 'SAVINGS');

-- Prakhar's Account (User ID 3)
INSERT INTO accounts (user_id, account_number, balance, account_type) 
VALUES (3, '1000000002', 300000.00, 'SAVINGS');

-- Nidhi's Account (User ID 4)
INSERT INTO accounts (user_id, account_number, balance, account_type) 
VALUES (4, '1000000003', 400000.00, 'SAVINGS');

-- Krishna's Account (User ID 5)
INSERT INTO accounts (user_id, account_number, balance, account_type) 
VALUES (5, '1000000004', 200000.00, 'SAVINGS');
