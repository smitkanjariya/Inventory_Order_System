-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 01, 2026 at 07:53 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `inventory_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `cart`
--

CREATE TABLE `cart` (
  `cart_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cart`
--

INSERT INTO `cart` (`cart_id`, `user_id`, `product_id`, `quantity`, `added_at`) VALUES
(18, 6, 15, 20, '2026-03-31 18:51:29');

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(100) NOT NULL,
  `manager_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `category_name`, `manager_id`) VALUES
(9, 'clothes', 7),
(10, 'watches', 7),
(11, 'bracelet', 8),
(12, 'Ring', 8),
(13, 'Earrings', 8),
(14, 'Necklace', 8),
(15, 'Kurti', 7);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `order_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` varchar(20) DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `user_id`, `order_date`, `status`, `total_amount`) VALUES
(1, 6, '2026-03-31 12:19:53', 'Completed', 14000.00),
(2, 6, '2026-03-31 13:26:02', 'Completed', 18000.00),
(3, 6, '2026-03-31 13:26:03', 'Approved', 40000.00),
(4, 6, '2026-03-31 13:27:08', 'Approved', 40000.00),
(5, 6, '2026-03-31 14:17:09', 'Approved', 2000.00),
(6, 6, '2026-03-31 14:31:17', 'Approved', 2000.00),
(7, 6, '2026-03-31 14:53:38', 'Completed', 28000.00),
(8, 6, '2026-03-31 14:55:12', 'Approved', 167300.00),
(9, 9, '2026-03-31 16:52:35', 'Completed', 2000.00),
(10, 9, '2026-03-31 16:54:22', 'Approved', 30000.00),
(11, 9, '2026-03-31 17:05:34', 'Approved', 4000.00),
(12, 9, '2026-03-31 19:28:43', 'Approved', 2000.00),
(13, 9, '2026-03-31 19:36:24', 'Approved', 2000.00),
(14, 9, '2026-03-31 19:41:58', 'Approved', 2000.00),
(15, 9, '2026-03-31 19:49:01', 'Approved', 2000.00),
(16, 9, '2026-03-31 19:54:40', 'Approved', 2000.00),
(17, 9, '2026-03-31 19:56:35', 'Approved', 2000.00),
(18, 9, '2026-04-01 05:49:24', 'Approved', 30300.00);

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`order_item_id`, `order_id`, `product_id`, `quantity`, `price`) VALUES
(1, 1, 11, 20, 700.00),
(2, 2, 12, 6, 3000.00),
(3, 3, 13, 10, 4000.00),
(4, 4, 13, 10, 4000.00),
(5, 5, 10, 5, 400.00),
(6, 6, 10, 5, 400.00),
(7, 7, 11, 40, 700.00),
(8, 8, 11, 239, 700.00),
(9, 9, 10, 5, 400.00),
(10, 10, 12, 10, 3000.00),
(11, 11, 10, 10, 400.00),
(12, 12, 10, 5, 400.00),
(13, 13, 10, 5, 400.00),
(14, 14, 10, 5, 400.00),
(15, 15, 10, 5, 400.00),
(16, 16, 10, 5, 400.00),
(17, 17, 10, 5, 400.00),
(18, 18, 16, 10, 3030.00);

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `payment_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('PENDING','COMPLETED','FAILED') DEFAULT 'PENDING',
  `payment_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `manager_id` int(11) NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `razorpay_payment_id` varchar(100) DEFAULT NULL,
  `razorpay_order_id` varchar(100) DEFAULT NULL,
  `request_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`payment_id`, `customer_id`, `total_amount`, `status`, `payment_date`, `manager_id`, `payment_method`, `razorpay_payment_id`, `razorpay_order_id`, `request_id`) VALUES
(2, 6, 14000.00, 'PENDING', '2026-03-31 13:24:49', 7, NULL, NULL, NULL, NULL),
(3, 6, 98000.00, 'FAILED', '2026-03-31 14:12:51', 8, 'Razorpay', 'pay_SXrTZZf8vXm2F1', 'order_SXrSF8JvQWcB1d', NULL),
(4, 9, 6000.00, 'COMPLETED', '2026-03-31 17:07:18', 7, 'Razorpay', 'pay_SXuRgdH1yaWASm', 'order_SXuQvcldHB9NHP', NULL),
(5, 6, 45000.00, 'COMPLETED', '2026-03-31 18:55:34', 8, NULL, NULL, NULL, NULL),
(6, 9, 8000.00, 'COMPLETED', '2026-03-31 19:35:19', 7, 'Razorpay', 'pay_SXwy3CZWSWS4Dz', 'order_SXwxosVvuyqSkD', NULL),
(7, 9, 14000.00, 'COMPLETED', '2026-03-31 19:53:02', 7, 'Razorpay', 'pay_SXxGkOTGtKH872', 'order_SXxGZIZOWiPt0y', NULL),
(8, 9, 30000.00, 'COMPLETED', '2026-04-01 05:44:12', 8, 'Razorpay', 'pay_SY7LFFiNcmgdhT', 'order_SY7KvmrubHIlxq', 18);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int(11) NOT NULL,
  `stock` int(11) DEFAULT 0,
  `reorder_level` int(11) NOT NULL,
  `category_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `min_order_quantity` int(11) DEFAULT 1 COMMENT 'Minimum quantity per order (wholesale)',
  `product_image` varchar(500) DEFAULT NULL COMMENT 'Product image URL or path',
  `manager_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `name`, `description`, `price`, `quantity`, `stock`, `reorder_level`, `category_id`, `created_at`, `min_order_quantity`, `product_image`, `manager_id`) VALUES
(10, 'manes watch', 'RADO', 400.00, 0, 0, 0, 10, '2026-03-31 12:04:06', 5, 'ef43dd07-3693-4a01-8ef9-acb382f8c95b.webp', 7),
(11, 'women dress', 'zara', 700.00, 201, 201, 10, 9, '2026-03-31 12:05:03', 20, '09b2a30a-0d75-4da5-9818-2e6a2112d24a.jpeg', 7),
(12, 'Embellished Diamond and Yellow Gold Ring for Men', 'Showcase your personality with this stunning 18Kt yellow gold ring embellished with a square center of diamonds', 3000.00, 84, 84, 0, 12, '2026-03-31 12:08:59', 10, 'da1ac707-36e7-4390-b11e-17e13364f062.webp', 8),
(13, 'Glimmering Leaf Design Diamond Bracelet', 'This gorgeous leaves pattern bracelet in gold studded with diamonds is simple and gently evocative', 4000.00, 280, 280, 0, 11, '2026-03-31 12:10:21', 10, '35235e31-71ed-485c-8a21-f7ed8872d62b.webp', 8),
(14, 'braclete', 'designer braclete', 2000.00, 100, 100, 0, 11, '2026-03-31 17:24:26', 10, '4ef230ad-afb1-4f7a-80b8-51b1d30f1680.webp', 8),
(15, 'Timeless Sparkle – Diamond Earrings Gold', 'Diamond Earrings Gold', 2250.00, 300, 280, 0, 13, '2026-03-31 17:37:59', 20, '8d5663ee-9b7a-4ea8-99a1-4b3c93815e23.jpg', 8),
(16, 'Pendant Necklace', 'Simple Emerald Pendant Necklace', 3030.00, 490, 490, 0, 14, '2026-03-31 17:40:31', 10, '76d9f746-1a85-40b9-914e-4084386b217e.jpg', 8),
(17, 'Shirtdress', 'Shirtdress clothes', 5000.00, 400, 400, 0, 15, '2026-03-31 17:44:44', 20, '2de89de8-1292-499b-a57f-db779c14f022.jpg', 7),
(18, 'Kurti Dress', 'Simple Dress', 3000.00, 400, 400, 0, 15, '2026-03-31 17:46:19', 12, '669be02c-518e-43e9-a436-820ec3cf48a5.jpg', 7);

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `report_id` int(11) NOT NULL,
  `report_type` varchar(50) DEFAULT NULL,
  `generated_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `customer_id` int(11) DEFAULT NULL,
  `manager_id` int(11) DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `payment_status` varchar(20) DEFAULT NULL,
  `payment_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reports`
--

INSERT INTO `reports` (`report_id`, `report_type`, `generated_date`, `customer_id`, `manager_id`, `total_amount`, `payment_status`, `payment_id`) VALUES
(1, 'Payment Pending', '2026-03-31 13:24:49', 6, 7, 14000.00, 'PENDING', NULL),
(2, 'Payment Completed', '2026-03-31 13:24:49', 6, 7, 14000.00, 'COMPLETED', 2),
(3, 'Payment Pending', '2026-03-31 14:12:51', 6, 8, 98000.00, 'PENDING', NULL),
(4, 'Payment Completed', '2026-03-31 14:12:51', 6, 8, 98000.00, 'COMPLETED', 3),
(5, 'Payment Pending', '2026-03-31 17:07:18', 9, 7, 6000.00, 'PENDING', NULL),
(6, 'Payment Completed', '2026-03-31 17:07:18', 9, 7, 6000.00, 'COMPLETED', 4),
(7, 'Payment Pending', '2026-03-31 19:35:19', 9, 7, 8000.00, 'PENDING', NULL),
(8, 'Payment Completed', '2026-03-31 19:35:19', 9, 7, 8000.00, 'COMPLETED', 6),
(9, 'Payment Pending', '2026-04-01 05:44:12', 9, 8, 30000.00, 'PENDING', NULL),
(10, 'Payment Completed', '2026-04-01 05:44:12', 9, 8, 30000.00, 'COMPLETED', 8);

-- --------------------------------------------------------

--
-- Table structure for table `request`
--

CREATE TABLE `request` (
  `request_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `status` enum('PENDING','ACCEPTED','REJECTED','PAID') NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `manager_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `request`
--

INSERT INTO `request` (`request_id`, `customer_id`, `product_id`, `quantity`, `status`, `created_at`, `manager_id`) VALUES
(7, 6, 11, 20, 'ACCEPTED', '2026-03-31 12:16:03', 7),
(8, 6, 13, 10, 'ACCEPTED', '2026-03-31 12:16:03', 8),
(9, 6, 12, 6, 'ACCEPTED', '2026-03-31 12:16:47', 8),
(10, 6, 13, 10, 'ACCEPTED', '2026-03-31 13:27:01', 8),
(11, 6, 12, 10, 'REJECTED', '2026-03-31 13:28:03', 8),
(12, 6, 10, 5, 'ACCEPTED', '2026-03-31 14:16:52', 7),
(13, 6, 10, 5, 'ACCEPTED', '2026-03-31 14:31:10', 7),
(14, 6, 13, 10, 'REJECTED', '2026-03-31 14:52:27', 8),
(15, 6, 11, 40, 'ACCEPTED', '2026-03-31 14:52:27', 7),
(16, 6, 11, 239, 'ACCEPTED', '2026-03-31 14:55:03', 7),
(17, 9, 10, 5, 'PAID', '2026-03-31 16:52:03', 7),
(18, 9, 12, 10, 'PAID', '2026-03-31 16:52:03', 8),
(19, 9, 10, 10, 'PAID', '2026-03-31 17:04:50', 7),
(20, 9, 10, 5, 'PAID', '2026-03-31 19:28:29', 7),
(21, 9, 10, 5, 'PAID', '2026-03-31 19:36:16', 7),
(22, 9, 10, 5, 'PAID', '2026-03-31 19:41:36', 7),
(23, 9, 10, 5, 'PAID', '2026-03-31 19:48:55', 7),
(24, 9, 10, 5, 'PAID', '2026-03-31 19:54:35', 7),
(25, 9, 10, 5, 'PAID', '2026-03-31 19:56:15', 7),
(26, 9, 16, 10, 'ACCEPTED', '2026-04-01 05:49:17', 8);

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`) VALUES
(1, 'Admin'),
(4, 'Customer'),
(2, 'Manager'),
(3, 'Staff');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(500) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Active',
  `role_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `organization_name` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `name`, `email`, `password`, `phone`, `address`, `profile_picture`, `status`, `role_id`, `created_at`, `organization_name`) VALUES
(1, 'Admin', 'admin@gmail.com', 'PBKDF2WithHmacSHA256:2048:i38YvUKGylFzhqZKSy6Zs3k0mJTHHbQysWmMYNZykNQ=:JIpa9UWtBDoKhPEsoZR1h5IloDUmPp4M6O6bIZdEMsw=', '123456789', '23,xyz abc', NULL, 'Active', 1, '2026-03-29 10:28:17', 'Admin Site'),
(6, 'smit kanajriya', 'smit@gmail.com', 'PBKDF2WithHmacSHA256:2048:QMhfJLRqQeTv+xg4p8z1lVGwxeeNXx0fPxcYkILdOyY=:9X3OXpiffezCuwUHNg3xEJG5NJZL8AcVYN775pGubS4=', '8780378779', '', NULL, 'Active', 4, '2026-03-29 12:55:34', 'aaaa'),
(7, 'hitu', 'hitu@gmail.com', 'PBKDF2WithHmacSHA256:2048:hlp1MooH33mAypV/TlCaMcbHny/8do8O6MaYkKQw3s0=:we+ATOOUrLKd49QspLXU8PpVb3ysNWC34nSHKBvwMkQ=', '', '', NULL, 'Active', 2, '2026-03-29 12:56:19', ''),
(8, 'dhruvi', 'dhruvi@gmail.com', 'PBKDF2WithHmacSHA256:2048:STv9mt06vCuUY09ET3sRFQpUxn2IwtZaub/RO9aM9w8=:VdTSK31x9y5w/6gwPrgHoMkf0CNU4loWXwSRiOqjfro=', '', '', NULL, 'Active', 2, '2026-03-29 13:23:48', ''),
(9, 'sam', 'sam@gmail.com', 'PBKDF2WithHmacSHA256:2048:K5g4gEYYZzZvEtOUa2PC037DqiqHKKh+UQ1KApJLI5k=:rOCWmE2FHWmq1tyjpCd00XJ9gUSXcSpekWWALGSm8z0=', '8780378779', 'vesu', NULL, 'Active', 4, '2026-03-31 16:50:41', 'fashion design');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`cart_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD KEY `fk_categories_manager` (`manager_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`payment_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `fk_payments_manager` (`manager_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `category_id` (`category_id`),
  ADD KEY `fk_products_manager` (`manager_id`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `reports_ibfk_customer` (`customer_id`),
  ADD KEY `reports_ibfk_manager` (`manager_id`);

--
-- Indexes for table `request`
--
ALTER TABLE `request`
  ADD PRIMARY KEY (`request_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `fk_request_manager` (`manager_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`),
  ADD UNIQUE KEY `role_name` (`role_name`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `role_id` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `cart`
--
ALTER TABLE `cart`
  MODIFY `cart_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `report_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `request`
--
ALTER TABLE `request`
  MODIFY `request_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `cart`
--
ALTER TABLE `cart`
  ADD CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`);

--
-- Constraints for table `categories`
--
ALTER TABLE `categories`
  ADD CONSTRAINT `fk_categories_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  ADD CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`);

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `fk_products_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`);

--
-- Constraints for table `reports`
--
ALTER TABLE `reports`
  ADD CONSTRAINT `reports_ibfk_customer` FOREIGN KEY (`customer_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `reports_ibfk_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `request`
--
ALTER TABLE `request`
  ADD CONSTRAINT `request_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `request_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
