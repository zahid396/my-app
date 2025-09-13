<?php

// Debug mode
define('DEBUG_MODE', false);

// Application settings
define('APP_NAME', 'eSports Ekattor');
define('APP_VERSION', '1.0.0');

// API endpoints and URLs
define('BASE_URL', 'https://api.esportsekattor.com'); // Base URL for navigation
define('LARAVEL_API_URL', 'https://api.esportsekattor.com/api/payment/callback'); // Laravel API endpoint

// Payment gateway numbers
define('BKASH_NUMBER', '01930119616'); // Your bkash number
define('NAGAD_NUMBER', '01930119616'); // Your nagad number
define('ROCKET_NUMBER', '01930119616'); // Your rocket number
define('UPAY_NUMBER', '01930119616'); // Your upay number

// Security settings
define('ENCRYPTION_KEY', 'esports-ekattor-secure-key-2024'); // Change this in production
define('SESSION_LIFETIME', 3600); // 1 hour
define('TWIXO_SECRET', 'esports-ekattor-twixo-secret-key'); // Webhook secret (must match Laravel .env)
define('LARAVEL_WEBHOOK_SECRET', 'esports-ekattor-twixo-secret-key'); // Laravel webhook secret

// Set timezone to Dhaka
date_default_timezone_set('Asia/Dhaka');

// Database configuration
$servername = "localhost";
$username = "esports_ekattor_user"; // Your database username
$password = "secure_password_2024"; // Your database password
$dbname = "esports_ekattor"; // Your database name

try {
    // Establish a database connection with PDO
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $conn->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

    // Create payments table if not exists
    $createTable = "
    CREATE TABLE IF NOT EXISTS payments (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id VARCHAR(255) NOT NULL,
        transaction_id VARCHAR(255) UNIQUE NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        payment_method VARCHAR(50) NOT NULL,
        status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_user_id (user_id),
        INDEX idx_transaction_id (transaction_id),
        INDEX idx_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ";
    
    $conn->exec($createTable);

} catch (PDOException $e) {
    // Handle connection errors
    error_log("Connection failed: " . $e->getMessage());
    die("Database connection error.");
}

function get_client_ip(): string {
    if (!empty($_SERVER['HTTP_CLIENT_IP'])) return $_SERVER['HTTP_CLIENT_IP'];
    if (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) return $_SERVER['HTTP_X_FORWARDED_FOR'];
    if (!empty($_SERVER['HTTP_X_FORWARDED'])) return $_SERVER['HTTP_X_FORWARDED'];
    if (!empty($_SERVER['HTTP_FORWARDED_FOR'])) return $_SERVER['HTTP_FORWARDED_FOR'];
    if (!empty($_SERVER['HTTP_FORWARDED'])) return $_SERVER['HTTP_FORWARDED'];
    if (!empty($_SERVER['REMOTE_ADDR'])) return $_SERVER['REMOTE_ADDR'];
    return 'UNKNOWN';
}

?>