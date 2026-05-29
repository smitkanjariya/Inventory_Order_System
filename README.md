# 🛒 Inventory & Order Management System

A comprehensive enterprise-level inventory and order management system built with Jakarta EE 10, JSF, and MySQL.

## 🚨 IMPORTANT - READ THIS FIRST

**Your project has critical database errors that prevent it from running!**

### Quick Fix (2 minutes):
```bash
# Run this in MySQL:
mysql -u root -p inventory_db < database_fixes.sql
```

**Then configure your datasource and deploy!**

For detailed instructions, see: **[QUICK_START.md](QUICK_START.md)**

---

## 📋 Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Setup Instructions](#setup-instructions)
- [User Roles](#user-roles)
- [API Endpoints](#api-endpoints)
- [Troubleshooting](#troubleshooting)

---

## ✨ Features

### 👤 User Management
- Role-based authentication (Admin, Manager, Staff, Customer)
- PBKDF2 password hashing
- User CRUD operations (Admin only)
- Profile management

### 📦 Product Management
- Product catalog with categories
- Stock tracking (quantity + inventory transactions)
- Low stock alerts
- Manager-specific product assignment
- Product search and filtering

### 🛍️ Order Management
- Customer order placement
- Order approval workflow (Manager)
- Order status tracking
- Order history
- Order items with pricing

### 💳 Payment System
- Customer-to-Manager payments
- Payment status tracking (Pending, Completed, Failed)
- Payment history

### 📋 Request System
- Customers can request products from Managers
- Manager approval/rejection workflow
- Request status tracking

### 🛒 Shopping Cart
- Add/remove products
- Quantity management
- Persistent cart storage

### 📊 Inventory Management
- Stock in/out tracking
- Inventory transaction history
- Low stock alerts
- Reorder level management

### 📈 Reports
- Sales reports
- Inventory reports
- Low stock reports
- Payment reports

---

## 🛠️ Technology Stack

### Backend
- **Jakarta EE 10** (Enterprise Java)
- **EJB 4.0** (Business Logic)
- **JPA 3.1** (Persistence)
- **Jakarta Security** (Authentication & Authorization)
- **JAX-RS** (REST APIs)

### Frontend
- **JSF 4.0** (JavaServer Faces)
- **PrimeFaces 13.0** (UI Components)
- **HTML5/CSS3**

### Database
- **MySQL 8.0+** / MariaDB

### Build & Deploy
- **Maven 3.6+**
- **GlassFish 7+** or **WildFly 27+**
- **Java 11+**

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (JSF Pages + Managed Beans)            │
│  - customer/dashboard.xhtml             │
│  - manager/dashboard.xhtml              │
│  - admin/dashboard.xhtml                │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Business Layer                  │
│  (EJB Services)                         │
│  - UserService                          │
│  - ProductService                       │
│  - OrderService                         │
│  - PaymentService                       │
│  - ManagerService                       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Persistence Layer               │
│  (JPA Entities)                         │
│  - Users, Products, Orders              │
│  - Payments, Manager, Request           │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Database Layer                  │
│  (MySQL)                                │
│  - 12 tables with relationships         │
└─────────────────────────────────────────┘
```

### Design Patterns Used
- **MVC** (Model-View-Controller)
- **DAO** (Data Access Object via EJB)
- **Dependency Injection** (CDI)
- **Session Facade** (EJB Services)
- **Template Method** (JSF Templates)

---

## 🗄️ Database Schema

### Tables (12 total)
1. **roles** - User roles
2. **users** - User accounts
3. **manager** - Manager-specific data
4. **categories** - Product categories
5. **products** - Product catalog
6. **orders** - Customer orders
7. **order_items** - Order line items
8. **payments** - Payment transactions
9. **request** - Customer requests to managers
10. **cart** - Shopping cart items
11. **inventory** - Stock transactions
12. **reports** - Generated reports

See **[DATABASE_DIAGRAM.md](DATABASE_DIAGRAM.md)** for detailed relationships.

---

## 🚀 Setup Instructions

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+ or MariaDB
- GlassFish 7+ or WildFly 27+

### Step 1: Fix Database
```bash
# Option A: Keep existing data
mysql -u root -p inventory_db < database_fixes.sql

# Option B: Fresh start (recommended)
mysql -u root -p < complete_schema.sql
```

### Step 2: Configure Application Server

#### GlassFish:
```bash
# Create connection pool
asadmin create-jdbc-connection-pool \
  --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
  --restype javax.sql.DataSource \
  --property user=root:password=YOUR_PASSWORD:serverName=localhost:portNumber=3306:databaseName=inventory_db \
  inventoryPool

# Create JDBC resource
asadmin create-jdbc-resource \
  --connectionpoolid inventoryPool \
  jdbc/inventoryDS

# Test connection
asadmin ping-connection-pool inventoryPool
```

### Step 3: Build & Deploy
```bash
cd Inventory_Order_System
mvn clean install
asadmin deploy target/Inventory_Order_System-1.0-SNAPSHOT.war
```

### Step 4: Access Application
```
URL: http://localhost:8080/Inventory_Order_System-1.0-SNAPSHOT/
```

---

## 👥 User Roles

### 🔴 Admin
**Access:** `/admin/*`

**Capabilities:**
- User management (CRUD)
- Product management (CRUD)
- Category management (CRUD)
- Order oversight
- Payment management
- Generate reports
- Create new users (including other admins)

**Default Login:**
- Email: `admin@gmail.com`
- Password: `admin123`

---

### 🟡 Manager
**Access:** `/manager/*`

**Capabilities:**
- Manage own products
- Manage categories
- View/approve/reject customer requests
- View orders
- Receive payments
- Generate reports
- Set organization name

**Default Login:**
- Email: `manager@gmail.com`
- Password: `manager123`

---

### 🟢 Staff
**Access:** `/staff/*`

**Capabilities:**
- View products
- Manage inventory (stock in/out)
- View low stock alerts
- Update stock levels

---

### 🔵 Customer
**Access:** `/customer/*`

**Capabilities:**
- Browse products
- Search and filter products
- Add to cart
- Place orders
- Make payments
- Send requests to managers
- View order history
- View payment status

**Default Login:**
- Email: `customer@gmail.com`
- Password: `customer123`

---

## 🌐 API Endpoints

### REST Resources

#### Products
```
GET  /api/products          - List all products
GET  /api/products/{id}     - Get product by ID
POST /api/products          - Create product
PUT  /api/products/{id}     - Update product
```

#### Categories
```
GET  /api/categories        - List all categories
POST /api/categories        - Create category
```

#### Orders
```
GET  /api/orders            - List all orders
GET  /api/orders/{id}       - Get order by ID
POST /api/orders            - Create order
```

#### Inventory
```
GET  /api/inventory         - List inventory
POST /api/inventory/stock   - Update stock
```

#### Dashboard
```
GET  /api/dashboard/stats   - Get dashboard statistics
```

#### Currency (External API)
```
GET  /api/currency/rates    - Get exchange rates
```

---

## 🐛 Troubleshooting

### Error: Table 'manager' doesn't exist
**Solution:** Run `database_fixes.sql`

### Error: Cannot find datasource
**Solution:** Configure `jdbc/inventoryDS` in application server

### Manager Dashboard Shows Errors
**Solution:** 
1. Verify `manager` table exists
2. Check if Manager record is created on login
3. Verify foreign keys in products/payments tables

### Customer Dashboard Not Loading
**Solution:**
1. Check if `products.stock` column exists
2. Verify categories table has data
3. Check browser console for JavaScript errors

### Login Fails
**Solution:**
1. Verify users table has data
2. Check password hash format
3. Verify role_id is set correctly
4. Check SecurityConfig datasource

### Build Fails
**Solution:**
```bash
mvn clean install -U
```

### Deployment Fails
**Solution:**
1. Check server logs
2. Verify datasource configuration
3. Ensure MySQL is running
4. Check if port 8080 is available

---

## 📁 Project Structure

```
Inventory_Order_System/
├── src/main/
│   ├── java/
│   │   ├── beans/              # JSF Managed Beans
│   │   ├── ejb/                # EJB Services
│   │   ├── entity/             # JPA Entities
│   │   ├── rest/               # REST Resources
│   │   ├── config/             # Security Config
│   │   ├── session/            # User Session
│   │   ├── filter/             # HTTP Filters
│   │   └── client/             # External API Clients
│   ├── resources/
│   │   └── META-INF/
│   │       └── persistence.xml
│   └── webapp/
│       ├── admin/              # Admin pages
│       ├── manager/            # Manager pages
│       ├── customer/           # Customer pages
│       ├── staff/              # Staff pages
│       ├── WEB-INF/
│       │   ├── templates/      # JSF templates
│       │   ├── web.xml
│       │   └── beans.xml
│       ├── resources/css/
│       ├── login.xhtml
│       └── register.xhtml
├── pom.xml
└── README.md
```

---

## 📚 Documentation Files

- **[ERROR_REPORT.md](ERROR_REPORT.md)** - Detailed error analysis
- **[QUICK_START.md](QUICK_START.md)** - Step-by-step setup guide
- **[DATABASE_DIAGRAM.md](DATABASE_DIAGRAM.md)** - Database relationships
- **[SUMMARY.md](SUMMARY.md)** - Summary of all fixes
- **database_fixes.sql** - Incremental database fixes
- **complete_schema.sql** - Complete database schema

---

## 🔒 Security Features

- **Authentication:** Jakarta Security with custom form login
- **Password Hashing:** PBKDF2WithHmacSHA256 (2048 iterations)
- **Authorization:** Role-based access control
- **Session Management:** 30-minute timeout
- **CSRF Protection:** Built into JSF
- **SQL Injection Prevention:** JPA parameterized queries

---

## 📝 License

This project is for educational purposes.

---

## 👨‍💻 Development

### Adding New Features
1. Create entity in `entity/` package
2. Create service interface in `ejb/` package
3. Implement service with `@Stateless`
4. Create managed bean in `beans/` package
5. Create XHTML page in appropriate folder
6. Update database schema

### Code Style
- Follow Jakarta EE naming conventions
- Use dependency injection (@EJB, @Inject)
- Keep beans stateless when possible
- Use named queries for common operations
- Follow MVC pattern strictly

---

## 🎯 Future Enhancements

- [ ] Email notifications
- [ ] PDF report generation
- [ ] Advanced analytics dashboard
- [ ] Multi-currency support
- [ ] Product images upload
- [ ] Barcode scanning
- [ ] Mobile responsive design
- [ ] Real-time notifications
- [ ] Audit logging
- [ ] Data export (Excel/CSV)

---

## 📞 Support

For issues and questions:
1. Check **[QUICK_START.md](QUICK_START.md)**
2. Review **[ERROR_REPORT.md](ERROR_REPORT.md)**
3. Check application server logs
4. Verify database schema matches entities

---

## ✅ Verification Checklist

After setup:
- [ ] Database has 12 tables
- [ ] JDBC resource `jdbc/inventoryDS` configured
- [ ] Application deploys without errors
- [ ] Login page loads
- [ ] Admin login works
- [ ] Manager login works
- [ ] Customer login works
- [ ] Customer dashboard displays products
- [ ] Manager dashboard loads without errors
- [ ] No errors in server logs

---

**Built with ❤️ using Jakarta EE 10**
