# 📌 Project Title – E-Commerce Multi-Vendor Backend

## 🔍 Overview
**E-Commerce Multi-Vendor** is a scalable backend system designed to support a multi-vendor marketplace. It enables seamless seller onboarding, product catalogue management, order processing, and admin-level analytics — empowering multiple store owners to sell products on a single platform under a unified API.

---

## 🚀 Tech Stack

- **Backend:** Java, Spring Boot (JPA, Security, Hibernate, REST API, JWT Authentication)  
- **Database:** MySQL  
- **DevOps & Deployment:** Docker, Render  
- **Version Control:** Git & GitHub  
- **Testing:** JUnit, Mockito (Controller & Service layer unit tests)

---

## 🌐 Live Demo

👉 [View the backend live on Render](https://ecommerce-multivendor-backend-xj9u.onrender.com/)

---

## 🛠 Core Features

- ✅ **Seller Onboarding & Management**  
  - Seller registration, login, profile updates  
  - Account status management (approve/suspend) via Admin endpoints

- ✅ **Product Management (Vendor)**  
  - Add, update, delete, and list products  
  - Assign products to specific categories  
  - Upload product images (API endpoints accept image URLs)

- ✅ **Category Management**  
  - Create, read, update, and delete product categories  
  - Admin endpoints to maintain home-categories for storefront UI

- ✅ **Order & Transaction Processing**  
  - Place and track orders from multiple vendors  
  - View order history, status updates, and payment details  
  - Generate simple transaction records for sellers and admin

- ✅ **Role-Based Access Control (RBAC)**  
  - **Seller**: Manage products, view orders, track transactions  
  - **Customer**: Browse products, place orders, view order status  
  - **Admin**: View total vendors, products, orders, users; suspend/activate accounts; manage categories

- ✅ **Secure Authentication & Authorization**  
  - User registration and login using **JWT**  
  - Passwords hashed with **BCrypt**  
  - Role checks on protected endpoints via **Spring Security**

- ✅ **RESTful API with Pagination & Filtering**  
  - Paginated product lists and order history  
  - Filter products by category, price range, and vendor

- ✅ **Health Check & Monitoring Endpoints**  
  - `/actuator/health` endpoint for uptime checks

- ✅ **Unit Testing**  
  - JUnit + Mockito tests for all **Controller** classes  
  - JUnit + Mockito tests for key **Service** classes (e.g., `ProductServiceImpl`, `VerificationServiceImpl`)

---
## 🔧 Installation & Local Setup

### 📁 1. Clone the Repository
```bash
git clone https://github.com/ramya-0315/ecommerce-multivendor-ramya-store.git
cd ecommerce-multivendor-ramya-store
