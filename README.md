# 🌱 Smart Plant Care System

## 📌 Overview

The **Smart Plant Care System** is a desktop application developed in **Java** using **JavaFX** that helps users efficiently monitor and manage the health of their plants. The system simulates a smart gardening environment by tracking essential plant parameters, generating automated alerts, scheduling maintenance tasks, and providing an intuitive user interface for plant care management.

This project was developed as part of my **Software Design & Architecture** course to demonstrate the practical implementation of **Object-Oriented Programming (OOP)** principles and several **Software Design Patterns** in a real-world application.

---

## 🚀 Features

* 🔐 Secure user authentication (Admin & User roles)
* 🌿 Plant management (Add, Edit, View, and Remove plants)
* 📊 Interactive dashboard with plant health statistics
* 🌡️ Real-time sensor monitoring simulation
* 💧 Watering schedule management
* ☀️ Sunlight exposure tracking
* 🌱 Fertilizer tracking and reminders
* 🐛 Pest and disease monitoring
* 🔔 Smart notification and alert system
* 📅 Automated task scheduling
* 📈 Plant growth monitoring
* 💾 File-based data persistence
* 👨‍💼 Administrative dashboard for managing users and system data
* 🎨 Modern JavaFX graphical user interface with animations and responsive components

---

## 🏗️ Software Architecture

The project follows an object-oriented architecture with modular components to ensure maintainability, scalability, and code reusability.

The application is organized into multiple layers, including:

* User Management
* Plant Management
* Sensor Monitoring
* Notification System
* File Persistence
* Dashboard & Analytics
* Administrative Module

---

## 🎯 Design Patterns Implemented

This project demonstrates the implementation of several Software Design Patterns:

### Singleton Pattern

Ensures a single shared instance for components such as:

* DataStore
* NotificationService

### Factory Pattern

Used to create different plant objects through:

* PlantFactory

### Observer Pattern

Implements an event-driven notification mechanism where observers receive updates whenever plant conditions change.

### Decorator Pattern

Extends plant functionality dynamically by adding:

* Fertilizer reminders
* Disease alerts
* Growth tracking

---

## 💻 Technologies Used

* Java
* JavaFX
* Object-Oriented Programming (OOP)
* File Handling
* Java Collections Framework
* Java Streams
* Java Time API

---

## 📊 System Functionalities

The application continuously monitors various plant health parameters, including:

* Soil moisture
* Temperature
* Humidity
* Sunlight exposure
* Fertilizer level
* Pest detection
* Disease detection
* Growth tracking
* Watering history

Based on these parameters, the system automatically determines each plant's health status:

* ✅ Healthy
* 💧 Needs Water
* ⚠️ Needs Attention
* 🚨 Critical

Whenever abnormal conditions are detected, automated alerts are generated to notify the user.

---

## 🎨 User Interface

The application provides a modern JavaFX desktop interface featuring:

* Interactive dashboard
* Animated transitions
* Responsive navigation
* Plant cards
* Statistical summaries
* Progress indicators
* Color-coded health status
* Administrative control panels

---

## 📁 Data Storage

Application data is stored locally using text files, including:

* User accounts
* Plant information
* Notifications
* Administrative logs

This enables persistent storage without requiring a database.

---

## 🎓 Learning Outcomes

This project enhanced my understanding of:

* Software Design & Architecture
* Design Patterns
* Object-Oriented Programming
* JavaFX Desktop Development
* Event-Driven Programming
* Modular Software Design
* File Handling
* Clean Code Practices
* User Interface Design
* Software Maintainability

---

## 📷 Project Preview

> Add screenshots or a demo GIF of the application here to showcase the dashboard, plant management interface, sensor monitoring, and alert system.

---

## 🔮 Future Improvements

* IoT sensor integration
* Cloud database support
* Mobile application version
* AI-based plant disease prediction
* Email and push notifications
* Weather API integration
* Plant image recognition
* Multi-user synchronization
* Analytics dashboard with charts
* REST API integration

---

## 👩‍💻 Author

**Laiba Sohail**

Software Engineering Student | AI & Data Enthusiast | Java Developer

Passionate about building scalable software solutions while continuously learning software architecture, artificial intelligence, and modern development technologies.

---

⭐ If you found this project interesting, consider giving it a star!
