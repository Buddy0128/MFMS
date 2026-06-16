-- MFMS Database Schema
-- MySQL 8+

CREATE DATABASE IF NOT EXISTS mfms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mfms;

CREATE TABLE IF NOT EXISTS admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    pin VARCHAR(4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_phone (phone_number),
    INDEX idx_admin_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    join_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_deposit DECIMAL(14,2) NOT NULL DEFAULT 0,
    current_loan_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_interest_paid DECIMAL(14,2) NOT NULL DEFAULT 0,
    imported_data BOOLEAN NOT NULL DEFAULT FALSE,
    last_imported_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member_phone (phone_number),
    INDEX idx_member_code (member_code),
    INDEX idx_member_status (status),
    INDEX idx_member_name (full_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS external_borrowers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_borrower_phone (phone_number),
    INDEX idx_borrower_name (full_name),
    INDEX idx_borrower_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS import_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    total_rows INT NOT NULL,
    new_members INT NOT NULL,
    updated_members INT NOT NULL,
    failed_records INT NOT NULL,
    imported_by VARCHAR(100) NOT NULL,
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_import_date (imported_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    contrib_month INT NOT NULL,
    contrib_year INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL DEFAULT 1000.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    UNIQUE KEY uk_member_month_year (member_id, contrib_month, contrib_year),
    INDEX idx_contribution_status (status),
    INDEX idx_contribution_date (contrib_year, contrib_month)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrower_type VARCHAR(20) NOT NULL,
    borrower_id BIGINT NOT NULL,
    loan_amount DECIMAL(12,2) NOT NULL,
    outstanding_amount DECIMAL(12,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    loan_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    imported_balance BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_loan_borrower (borrower_type, borrower_id),
    INDEX idx_loan_status (status),
    INDEX idx_loan_date (loan_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS principal_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    INDEX idx_principal_loan (loan_id),
    INDEX idx_principal_date (payment_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS interest_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    INDEX idx_interest_loan (loan_id),
    INDEX idx_interest_date (payment_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT,
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE SET NULL,
    INDEX idx_activity_admin (admin_id),
    INDEX idx_activity_date (created_at)
) ENGINE=InnoDB;
