# MediMinder

## 2. Project Description

MediMinder is a medication companion app that helps users manage one or more medicines, each with its own schedule. It warns users before their supply runs out, lets them keep a quick personal log of how they feel after taking medication, and produces a summary report they can bring to a doctor visit. The app is designed for people managing chronic conditions who need to keep track of several medicines at once.

## 3. Problem Being Addressed

Chronic patients who take multiple medications often:

- Forget doses, especially when schedules differ between medicines.
- Run out of supply unexpectedly, which leads to gaps in treatment.
- Lose track of how their body reacts to a treatment, making it hard to give their doctor useful information.

These issues can result in missed doses and gaps in care. MediMinder addresses them by centralizing medication schedules, supply tracking, and personal health notes in one place.

## 4. Project Objectives

**General Objective**
To develop MediMinder, a medication companion app that helps chronic patients stay on schedule with their medicines, avoid unexpected supply gaps, and keep organized records to share with their healthcare providers.

**Specific Objectives**

- Allow users to add and manage multiple medications, each with its own name, dosage, and schedule.
- Track remaining supply and alert users when doses are running low, including days of supply left and the projected run-out date.
- Provide an optional notes log where users can record how they feel after taking a medication, and flag recurring patterns.
- Support prescription scanning so medicine details can be captured from a photo and then reviewed and edited by the user before saving.
- Generate a doctor visit summary covering adherence, supply status, patterns, skipped doses, notes, allergies, and conditions.
- Help users locate pharmacies that carry their medicines, ranked by previously recorded stock status.

## 5. Target Users

- **Chronic patients** managing one or more daily medications (for example, blood pressure and diabetes medicines).
- **Caregivers or family members** who help organize a patient's medication schedule.
- **Patients preparing for doctor visits** who want a clear summary of their adherence and how they have been feeling.

## 6. Main Features

**6.1 Multiple Medication Support**
Add several medications, each with its own name, dosage, and dose times.

**6.2 Low Supply Alert**
Enter the number of pills or doses left. The app shows days of supply remaining and the projected run-out date based on daily doses, and warns the user when 3 days or less remain, even if the dose threshold has not been reached (for example, "Only 3 doses left, consider refilling soon").

**6.3 Simple Health Notes**
An optional field for quick feelings or notes after taking medication (for example, "dizzy," "fine," "nauseous"). If the same feeling appears after the same medicine at least twice and in 40% or more of its doses, it is flagged in the journal (for example, "Dizzy" after 3 of 5 doses of one medicine). This is a personal log, not medical tracking.

**6.4 Prescription Scanning (Meds Tab)**
Take a photo of a prescription, medical certificate, or medicine label. The app reads and fills in each medicine's name, dosage, doses per day, and quantity dispensed. The user reviews and edits all fields before saving. If a photo is unreadable, the app says so rather than guessing.

**6.5 Scan to Pharmacy (Experimental)**
After a scan, each medicine in the review step includes a "Where to get it" section listing saved pharmacies, ranked as follows:

1. Pharmacies where the medicine was previously marked In stock.
2. Pharmacies not yet checked for that medicine.
3. Pharmacies where the medicine was previously marked Out of stock.

*Note: this feature has not been fully tested.*

**6.6 Doctor Visit Summary**
One tap generates a report for the last 14, 30, or 90 days that can be copied. It includes:

- **Adherence:** doses marked Taken versus doses scheduled, per medication, with a percentage (for example, "Taken 52 of 60 (87%)"). Days before a medicine was added are not counted, and only doses whose times have already passed are included.
- **Supply:** doses left and approximately how many days they will last.
- **Patterns:** recurring feelings linked to specific medicines, as described in 6.3.
- **How I Felt:** a dated list of quick feelings and notes, with the medicine each one followed.
- **Skipped Doses:** a record of missed doses.
- **Allergies and Conditions:** included if the user has entered them.

**6.7 Find Nearby Pharmacies**
Opens a map search for pharmacies carrying a given medicine. Adding a city or neighborhood under "My Pharmacies" improves search results.

**6.8 "Hard to Find" Switch**
For medicines that are often out of stock, this option raises the low-supply warning to 14 days in advance.

## 7. Technologies Used

| Technology | Purpose |
|---|---|
| [TO BE FILLED] | [Language / framework for the app] |
| [TO BE FILLED] | [Data storage for medications, dose logs, and pharmacies] |
| [TO BE FILLED] | [Image capture and text recognition for prescription scanning] |
| [TO BE FILLED] | [Map and pharmacy search service] |

*Replace the placeholders above with the stack actually used in the project.*

## 8. Team Members and Roles

| Team Member | Role |
|---|---|
| [TO BE FILLED] | Project Lead |
| Marana, Keenan | [TO BE FILLED] |
| Rocafort, Kelvin | [TO BE FILLED] |
| Gulay, Elric | [TO BE FILLED] |

## 9. OOP Concepts Demonstrated

*Suggested mapping based on the app's features. Confirm against the actual code before submission.*

- **Encapsulation:** Medication, DoseLog, and Pharmacy classes keep their data private and expose it through methods (for example, a Medication's remaining supply is updated only through a dedicated method).
- **Inheritance:** a base class such as Reminder or Alert can be extended by LowSupplyAlert and StockAlert.
- **Polymorphism:** different summary sections (adherence, supply, patterns) implement a common interface such as ReportSection and are generated the same way.
- **Abstraction:** a common interface for pharmacy lookup allows the map search to be swapped without changing the rest of the app.
- **Composition:** a Schedule object is composed into each Medication, and a Report is composed of multiple ReportSection objects.

## 10. Instructions for Running the Program

[TO BE FILLED]

Suggested structure:

1. Prerequisites (required software and versions).
2. Clone or download the repository: `git clone [repository-url]`
3. Install dependencies: `[install command]`
4. Configure any required settings or API keys.
5. Run the application: `[run command]`
6. Open the app at `[local address or URL]`.

## 11. Sample Screenshots

[TO BE FILLED: add screenshots of the medication list, low supply alert, health notes journal, prescription scan review, and doctor visit summary.]

## 12. Known Limitations

- **Not a medical device.** MediMinder is a personal organizer and does not provide medical advice. Users should follow their doctor's instructions.
- **Scan accuracy.** Text read from prescription photos may be incorrect, so all scanned details must be reviewed before saving.
- **Pharmacy stock information is user-reported.** Stock ranking depends on what the user has previously marked, so it may be outdated.
- **Scan to Pharmacy is untested.** This feature has not been fully tested.
- **Pattern detection is simple.** Patterns are flagged using a fixed rule (at least 2 occurrences and 40% of doses) and do not account for other causes.
- **Map search depends on external results.** Pharmacy search quality depends on the map service and on the city or neighborhood the user provides.

## 13. Future Improvements

- Complete testing of the Scan to Pharmacy feature and improve stock ranking.
- Add reminders and notifications for upcoming doses.
- Improve prescription scanning accuracy and support more label formats.
- Allow export of doctor visit summaries as a file (for example, PDF).
- Support multiple user profiles for caregivers managing more than one patient.

## 14. Project Contributors

- Marana, Keenan
- Rocafort, Kelvin
- Gulay, Elric
