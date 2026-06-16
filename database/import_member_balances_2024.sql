USE mfms;

START TRANSACTION;

DELETE FROM activity_logs;
DELETE FROM interest_payments;
DELETE FROM principal_payments;
DELETE FROM loans;
DELETE FROM contributions;
DELETE FROM external_borrowers;
DELETE FROM import_history;
DELETE FROM members;

INSERT INTO admins (name, phone_number, pin, status)
VALUES
    ('Admin One', '9999999991', '1234', 'ACTIVE'),
    ('Admin Two', '9999999992', '5678', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    pin = VALUES(pin),
    status = VALUES(status);

INSERT INTO members (
    member_code,
    full_name,
    phone_number,
    join_date,
    status,
    total_deposit,
    current_loan_amount,
    total_interest_paid,
    imported_data,
    last_imported_at
)
VALUES
    ('M001', 'Mahesh Kharpadiya', '9000000001', '2024-01-01', 'ACTIVE', 30000.00, 86000.00, 7230.00, TRUE, NOW()),
    ('M002', 'Aajip Kharpadiya', '9000000002', '2024-01-01', 'ACTIVE', 29000.00, 0.00, 1500.00, TRUE, NOW()),
    ('M003', 'Mukesh Kharpadiya', '9000000003', '2024-01-01', 'ACTIVE', 29000.00, 0.00, 230.00, TRUE, NOW()),
    ('M004', 'Ashit Kharpadiya', '9000000004', '2024-01-01', 'ACTIVE', 29000.00, 35000.00, 7095.00, TRUE, NOW()),
    ('M005', 'Yogesh Kharpadiya', '9000000005', '2024-01-01', 'ACTIVE', 29000.00, 37000.00, 4320.00, TRUE, NOW()),
    ('M006', 'Kurshan Kharpadiya', '9000000006', '2024-01-01', 'ACTIVE', 29000.00, 0.00, 4720.00, TRUE, NOW()),
    ('M007', 'Alvish Kharpadiya', '9000000007', '2024-01-01', 'ACTIVE', 29000.00, 52000.00, 3500.00, TRUE, NOW()),
    ('M008', 'Jamshu Kharpadiya', '9000000008', '2024-01-01', 'ACTIVE', 29000.00, 25000.00, 4150.00, TRUE, NOW()),
    ('M009', 'Divyesh Kharpadiya', '9000000009', '2024-01-01', 'ACTIVE', 29000.00, 40000.00, 1280.00, TRUE, NOW()),
    ('M010', 'Shayresh Kharpadiya', '9000000010', '2024-01-01', 'ACTIVE', 23000.00, 65000.00, 4350.00, TRUE, NOW()),
    ('M011', 'Dilip Kharpadiya', '9000000011', '2024-01-01', 'ACTIVE', 29000.00, 70000.00, 2300.00, TRUE, NOW()),
    ('M012', 'Ankit Kharpadiya', '9000000012', '2024-01-01', 'ACTIVE', 29000.00, 45000.00, 3250.00, TRUE, NOW()),
    ('M013', 'Manesh Kharpadiya', '9000000013', '2024-01-01', 'ACTIVE', 29000.00, 0.00, 390.00, TRUE, NOW()),
    ('M014', 'Vishal Kharpadiya', '9000000014', '2024-01-01', 'ACTIVE', 29000.00, 18000.00, 1380.00, TRUE, NOW()),
    ('M015', 'Kishan Kharpadiya', '9000000015', '2024-01-01', 'ACTIVE', 29000.00, 10000.00, 2700.00, TRUE, NOW()),
    ('M016', 'Mahindra Valvi', '9000000016', '2024-01-01', 'ACTIVE', 29000.00, 22500.00, 3205.00, TRUE, NOW()),
    ('M017', 'Vinu Nagvasi', '9000000017', '2024-01-01', 'ACTIVE', 28000.00, 31000.00, 4240.00, TRUE, NOW()),
    ('M018', 'Rajal Valvi', '9000000018', '2024-01-01', 'ACTIVE', 29000.00, 67000.00, 4958.00, TRUE, NOW()),
    ('M019', 'Pravin Nagvasi', '9000000019', '2024-01-01', 'ACTIVE', 29000.00, 0.00, 1400.00, TRUE, NOW()),
    ('M020', 'Manilal Valvi', '9000000020', '2024-01-01', 'ACTIVE', 29000.00, 0.00, 0.00, TRUE, NOW());

INSERT INTO contributions (
    member_id,
    contrib_month,
    contrib_year,
    amount,
    status,
    payment_date
)
WITH RECURSIVE contribution_months AS (
    SELECT DATE('2024-01-01') AS month_start
    UNION ALL
    SELECT DATE_ADD(month_start, INTERVAL 1 MONTH)
    FROM contribution_months
    WHERE month_start < DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
)
SELECT
    m.id,
    MONTH(cm.month_start),
    YEAR(cm.month_start),
    1000.00,
    CASE
        WHEN TIMESTAMPDIFF(MONTH, DATE('2024-01-01'), cm.month_start) < FLOOR(m.total_deposit / 1000)
        THEN 'PAID'
        ELSE 'PENDING'
    END,
    CASE
        WHEN TIMESTAMPDIFF(MONTH, DATE('2024-01-01'), cm.month_start) < FLOOR(m.total_deposit / 1000)
        THEN LAST_DAY(cm.month_start)
        ELSE NULL
    END
FROM members m
CROSS JOIN contribution_months cm
WHERE m.status = 'ACTIVE';

INSERT INTO loans (
    borrower_type,
    borrower_id,
    loan_amount,
    outstanding_amount,
    interest_rate,
    loan_date,
    status,
    imported_balance
)
SELECT
    'MEMBER',
    m.id,
    d.current_loan_amount,
    d.current_loan_amount,
    1.00,
    '2024-01-01',
    'ACTIVE',
    TRUE
FROM members m
JOIN (
    SELECT 'M001' member_code, 86000.00 current_loan_amount UNION ALL
    SELECT 'M004', 35000.00 UNION ALL
    SELECT 'M005', 37000.00 UNION ALL
    SELECT 'M007', 52000.00 UNION ALL
    SELECT 'M008', 25000.00 UNION ALL
    SELECT 'M009', 40000.00 UNION ALL
    SELECT 'M010', 65000.00 UNION ALL
    SELECT 'M011', 70000.00 UNION ALL
    SELECT 'M012', 45000.00 UNION ALL
    SELECT 'M014', 18000.00 UNION ALL
    SELECT 'M015', 10000.00 UNION ALL
    SELECT 'M016', 22500.00 UNION ALL
    SELECT 'M017', 31000.00 UNION ALL
    SELECT 'M018', 67000.00
) d ON d.member_code = m.member_code;

INSERT INTO loans (
    borrower_type,
    borrower_id,
    loan_amount,
    outstanding_amount,
    interest_rate,
    loan_date,
    status,
    imported_balance
)
SELECT
    'MEMBER',
    m.id,
    0.00,
    0.00,
    1.00,
    '2024-01-01',
    'CLOSED',
    TRUE
FROM members m
WHERE m.member_code IN ('M002', 'M003', 'M006', 'M013', 'M019');

INSERT INTO interest_payments (loan_id, amount, payment_date)
SELECT
    l.id,
    d.total_interest_paid,
    '2024-12-31'
FROM loans l
JOIN members m ON m.id = l.borrower_id AND l.borrower_type = 'MEMBER'
JOIN (
    SELECT 'M001' member_code, 7230.00 total_interest_paid UNION ALL
    SELECT 'M002', 1500.00 UNION ALL
    SELECT 'M003', 230.00 UNION ALL
    SELECT 'M004', 7095.00 UNION ALL
    SELECT 'M005', 4320.00 UNION ALL
    SELECT 'M006', 4720.00 UNION ALL
    SELECT 'M007', 3500.00 UNION ALL
    SELECT 'M008', 4150.00 UNION ALL
    SELECT 'M009', 1280.00 UNION ALL
    SELECT 'M010', 4350.00 UNION ALL
    SELECT 'M011', 2300.00 UNION ALL
    SELECT 'M012', 3250.00 UNION ALL
    SELECT 'M013', 390.00 UNION ALL
    SELECT 'M014', 1380.00 UNION ALL
    SELECT 'M015', 2700.00 UNION ALL
    SELECT 'M016', 3205.00 UNION ALL
    SELECT 'M017', 4240.00 UNION ALL
    SELECT 'M018', 4958.00 UNION ALL
    SELECT 'M019', 1400.00
) d ON d.member_code = m.member_code;

INSERT INTO import_history (
    file_name,
    total_rows,
    new_members,
    updated_members,
    failed_records,
    imported_by
)
VALUES ('Screenshot 2026-06-11 225311.png', 20, 20, 0, 0, 'System');

INSERT INTO activity_logs (admin_id, action, description)
SELECT id, 'Imported Member Balances', 'Loaded 20 member balance rows from screenshot into MySQL'
FROM admins
WHERE phone_number = '9999999991'
LIMIT 1;

COMMIT;
