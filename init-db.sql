-- Database initialization script for Library Management System
-- This script runs automatically when the MySQL container starts

USE librarymanagement;

-- Set default storage engine
SET default_storage_engine=InnoDB;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS=1;

-- Create database user with privileges (if not exists)
-- This ensures library_user has all necessary permissions
GRANT ALL PRIVILEGES ON librarymanagement.* TO 'library_user'@'%';
FLUSH PRIVILEGES;

-- Initial data can be added here if needed
-- Example: INSERT INTO publishers VALUES (1, 'Publisher Name', 'Address', 'Phone');

-- Create indexes for better performance
-- These can be added here or via application migrations

-- End of initialization script
