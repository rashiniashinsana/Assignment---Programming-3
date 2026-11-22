# SmartStay Hotel Booking System
## CI6115 Programming III: Patterns and Algorithms
### Kingston University London – Coursework Submission Package

**System Name:** SmartStay Hotel Booking System  
**Module:** CI6115 Programming III (50% Coursework)  
**Technology Stack:** Java 17 LTS, Java Swing, Apache Maven, JUnit 5  
**Candidate Name:** `A V Rashini Ashinsana`  
**Kingston Student ID:** `2635746`  
**UML Classifier Prefix:** `2635746_`  
**Submission Date:** November 2026  

---

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
mvn test-compile
java -cp "target/classes;target/test-classes" lk.smartstay.Phase12FunctionalVerificationTest
java -cp "target/classes;target/test-classes" lk.smartstay.Phase11GUIRefinementTest
```
All 53 Phase 12 functional scenarios (TC01-TC53) and 95 Phase 11 tests execute with a 100% pass rate.

---

## Submission Package Contents (`docs/` & `data/`)
* **`docs/SmartStay_Coursework_Report_Justified.docx`**: Master Word report with justified text alignment, embedded production screenshots, and comprehensive academic justification.
* **`docs/SmartStay_Coursework_Report.md`**: Complete Markdown coursework report.
* **`docs/screenshots/`**: 8 high-resolution GUI screenshots (`01_dashboard.png` to `08_mainframe_overview.png`).
* **`data/smartstay.db`**: Relational database persistence file.
* **`data/smartstay_dump.sql`**: Auto-synchronized SQL export.
* **`docs/smartstay-class-diagram.puml` / `.svg`**: Full UML Class Diagram prefixed with `2635746_`.
* **`SmartStay_CI6115_Submission.zip`**: Complete submission archive.

---
*Kingston University London – CI6115 Programming III.*
