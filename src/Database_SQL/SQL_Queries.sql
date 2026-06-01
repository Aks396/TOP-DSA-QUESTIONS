-- ===========================================================================
-- DATABASE & SQL CODING QUESTIONS AND SOLUTIONS
-- Covers the most asked SQL questions from the Backend Interview Master Guide
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- 0. Schema Setup & Mock Data (PostgreSQL / MySQL Compatible)
-- ---------------------------------------------------------------------------

CREATE TABLE departments (
    dept_id INT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL
);

CREATE TABLE employees (
    emp_id INT PRIMARY KEY,
    emp_name VARCHAR(100) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    dept_id INT REFERENCES departments(dept_id)
);

CREATE TABLE users (
    user_id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- Insert Mock Data
INSERT INTO departments VALUES (1, 'Engineering'), (2, 'HR'), (3, 'Sales');

INSERT INTO employees VALUES 
(101, 'Alice', 95000.00, 1),
(102, 'Bob', 80000.00, 1),
(103, 'Charlie', 80000.00, 1), -- Same salary as Bob
(104, 'David', 60000.00, 2),
(105, 'Eve', 120000.00, 1);

INSERT INTO users VALUES 
(1, 'john_doe', 'john@example.com'),
(2, 'jane_smith', 'jane@example.com'),
(3, 'copycat', 'john@example.com'), -- Duplicate email
(4, 'duplicate_email_user', 'jane@example.com'); -- Duplicate email

-- ---------------------------------------------------------------------------
-- 1. How do you find duplicate rows in a table? (e.g., users with duplicate emails)
-- ---------------------------------------------------------------------------

-- Query to find duplicate email addresses and their counts
SELECT email, COUNT(*) as occurrence_count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- Query to get the full record details of duplicate users
SELECT * 
FROM users 
WHERE email IN (
    SELECT email 
    FROM users 
    GROUP BY email 
    HAVING COUNT(*) > 1
) 
ORDER BY email;

-- ---------------------------------------------------------------------------
-- 2. SQL Joins Comparisons (INNER, LEFT, RIGHT, FULL OUTER)
-- ---------------------------------------------------------------------------

-- INNER JOIN (Only returns matched rows in both tables)
SELECT e.emp_name, d.dept_name
FROM employees e
INNER JOIN departments d ON e.dept_id = d.dept_id;

-- LEFT JOIN (Returns all employees, even if they aren't assigned a department)
SELECT e.emp_name, d.dept_name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id;

-- RIGHT JOIN (Returns all departments, even if they have no employees assigned)
SELECT e.emp_name, d.dept_name
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;

-- FULL JOIN (Returns all rows from both tables, fills with NULLs where mismatched)
SELECT e.emp_name, d.dept_name
FROM employees e
FULL OUTER JOIN departments d ON e.dept_id = d.dept_id;

-- ---------------------------------------------------------------------------
-- 3. Window Functions for Rankings (Find N-th Highest Salary)
-- ---------------------------------------------------------------------------

-- 3.1 Finding the 2nd Highest Salary Overall (Standard Subquery)
SELECT MAX(salary) 
FROM employees 
WHERE salary < (SELECT MAX(salary) FROM employees);

-- 3.2 Finding Rankings per Department using Window Functions
-- ROW_NUMBER(): Unique sequential numbers (e.g., 1, 2, 3)
-- RANK(): Leaves gaps on ties (e.g., 1, 2, 2, 4)
-- DENSE_RANK(): Continuous numbers on ties (e.g., 1, 2, 2, 3)
SELECT emp_name, salary, dept_id,
       ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) as row_num,
       RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) as salary_rank,
       DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) as salary_dense_rank
FROM employees;

-- 3.3 Find 2nd Highest Salary in each department using DENSE_RANK()
WITH SalaryRankings AS (
    SELECT emp_name, salary, dept_id,
           DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) as rank_val
    FROM employees
)
SELECT emp_name, salary, dept_id
FROM SalaryRankings
WHERE rank_val = 2;

-- ---------------------------------------------------------------------------
-- 4. EXPLAIN / Query Optimizations
-- ---------------------------------------------------------------------------

-- How to analyze a query's execution plan
EXPLAIN ANALYZE 
SELECT * FROM employees WHERE salary > 75000.00;

-- Creating a B-Tree Index to optimize filtering queries
CREATE INDEX idx_employee_salary ON employees(salary);

-- Creating a Composite Index (Order of columns matters: left-prefix rule)
CREATE INDEX idx_emp_dept_salary ON employees(dept_id, salary);
-- Optimized for:
--   WHERE dept_id = 1 AND salary > 50000
--   WHERE dept_id = 1
-- Bypassed (not optimized) for:
--   WHERE salary > 50000 (violates left-prefix rule)
