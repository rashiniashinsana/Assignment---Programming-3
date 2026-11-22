# SmartStay Hotel Booking System
## CI6115 Programming III: Patterns and Algorithms
### Kingston University London – Coursework Submission Package

**System Name:** SmartStay Hotel Booking System  
**Module:** CI6115 Programming III (50% Coursework)  
**Technology Stack:** Java 17 LTS, Java Swing, Apache Maven, JUnit 5  
**Candidate Name:** `[A  V Rashini Ashinsana]`  
**Kingston Student ID:** `2635746`  



## How to Build and Run the System

### Prerequisites
* Java Development Kit (JDK) 17 or higher installed and on `PATH`.
* Apache Maven 3.8+ installed and on `PATH`.

### Compile the Application
```bash
mvn clean compile
```

### Run the Application

#### Option A: Graphical Desktop GUI (Java Swing)
```bash
java -cp target/classes lk.smartstay.Main
```
Or via Maven:
```bash
mvn exec:java -Dexec.mainClass="lk.smartstay.Main"
```

#### Option B: Interactive Terminal Menu (CLI)
```bash
java -cp target/classes lk.smartstay.SmartStayApp
```
Or via Maven:
```bash
mvn exec:java
```

### Run Automated Unit and Integration Test Suite
```bash
mvn test
```
All 12 test suites (565 assertions) will execute, verifying 100% pass rate across all modules.

---
*Kingston University London – CI6115 Programming III.*
