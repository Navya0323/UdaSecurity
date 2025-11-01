# 🛡️ CatPoint Home Security System

This modular Java application was developed as part of the **Java Developer Nanodegree at Udacity**. It simulates a home security system that integrates image processing, sensor logic, and a graphical user interface to monitor and respond to potential intrusions.

---

## 📌 Project Overview

The CatPoint system monitors a home environment using motion sensors and image recognition. Its goal is to detect unauthorized entries while intelligently ignoring common pets like cats.

The application is divided into three modules:

1. **Security Service**
    - Core logic for arming/disarming the system
    - Alarm state transitions
    - Sensor management and repository integration

2. **Image Service**
    - Interfaces with a mock image classifier
    - Simulates AWS Rekognition or fake classifier to detect cats

3. **Security GUI**
    - Java Swing-based interface
    - Allows users to monitor and control system states in real time

---

## ✅ Project Requirements Met

- Modular architecture using Java modules (`module-info.java`)
- Sensor and alarm logic implementation
- Image classification integration
- Responsive GUI for user interaction
- Maven-based build and reporting setup
- JUnit-based unit testing
- Static analysis with SpotBugs

---

## 🧠 System Capabilities

- Arm/disarm the security system
- Detect motion/contact sensor activity
- Use image classification to identify cats
- Trigger alarms based on combined sensor and image inputs
- Display system status and controls via GUI
- Generate test and analysis reports using Maven tools

---

## 🧱 Tech Stack

| Component        | Technology                     |
|------------------|-------------------------------|
| Language         | Java 11+                      |
| Build Tool       | Maven                         |
| UI Framework     | Java Swing                    |
| Testing          | JUnit 5, Maven Surefire       |
| Static Analysis  | SpotBugs                      |
| Modularization   | Java Modules (`module-info.java`) |

---

## 🧪 Unit Test Coverage

### `SecurityServiceTest.java`
- Validates alarm state transitions
- Tests sensor activation/deactivation logic
- Ensures correct behavior when cats are detected while armed
- Confirms image detection influences alarm state appropriately

### `AppTest.java` (image-service)
- Placeholder for future image service tests

---

## 📊 Reports and Analysis

All reports are generated and stored **only in the `security-service` module**:

- **JaCoCo Coverage Report**  
  `security-service/target/site/jacoco/index.html`

- **SpotBugs Static Analysis**  
  `security-service/target/spotbugs-build/spotbugs.html`

- **JUnit Test Results (Surefire)**  
  `security-service/target/surefire-reports/`

---

## 🚀 Running the Application
To run the packaged application:
```bash
java -jar security-service/target/security-service-1.0-SNAPSHOT-jar-with-dependencies.jar
