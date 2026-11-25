CREATE DATABASE MediminderDB;

USE MediminderDB;
CREATE TABLE Medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50),
    frequency VARCHAR(50),
    start_date DATE,
    end_date DATE,

);
CREATE TABLE Reminders (
    reminder_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    reminder_time TIME NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

CREATE TABLE Notes (
    note_id INT PRIMARY KEY AUTO_INCREMENT,
    note_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

CREATE TABLE SearchHistory (
    search_id INT PRIMARY KEY AUTO_INCREMENT,
    search_query VARCHAR(255) NOT NULL,
    search_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

Create Table MedicationHistory (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    taken_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

Create Table MedicationRefill (
    refill_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    refill_date DATE NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

CREATE TABLE MedicationInteractions (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id_1 INT,
    medicine_id_2 INT,
    interaction_description TEXT NOT NULL,
    FOREIGN KEY (medicine_id_1) REFERENCES Medicines(medicine_id),
    FOREIGN KEY (medicine_id_2) REFERENCES Medicines(medicine_id)
);

Create Table MedicationSideEffects (
    side_effect_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    side_effect_description TEXT NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

Create Table MedicationAllergies (
    allergy_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    allergy_description TEXT NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

Create Table MedicationDosageHistory (
    dosage_history_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    dosage VARCHAR(50) NOT NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

Create Table MedicationStorage (
    storage_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    storage_location VARCHAR(100) NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

Create Table MedicationRefillReminders (
    refill_reminder_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    reminder_time TIME NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);

Create Table MedicationRefillHistory (
    refill_history_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT,
    refill_date DATE NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicines(medicine_id)
);