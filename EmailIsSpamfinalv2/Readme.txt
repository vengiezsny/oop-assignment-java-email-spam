Email Spam Predictor

What This App Does
This Java application predicts whether an email is spam or not using a simple machine learning approach (frequency-based classifier). It features a graphical user interface (GUI) that allows users to predict, add data, retrain the model, and test accuracy. For this I used my data that is in the csv file.

Author: Vengie Legaspi  
Date: April 29, 2025

How It Works
The app examines five email features:

1. Subject Length: Short, Medium, or Long
2. Urgent Words: Yes or No
3. Sender Domain: Trusted, Unknown, or Suspicious
4. Attachments: Yes or No
5. Links: None, Few, or Many

Java Classes

1. `Email`
   - Stores the five features of an email
   - Provides methods to access features
   - Keeps data secure with immutable design

2. `SpamClassifier` (Interface)
   - Defines methods all spam classifiers must implement
   - Includes training, prediction, and probability methods
   - Allows for different classifier types

3. `FrequencyBasedSpamClassifier`
   - Implements the SpamClassifier interface
   - Counts feature combinations in spam/non-spam emails
   - Calculates spam probability

4. `DatasetManager`
   - Loads and saves email data from CSV
   - Adds new emails to dataset
   - Resets data when needed

5. `EmailSpamPredictorGUI`
   - Main user interface with tabs
   - Handles user input and displays results
   - Connects classifier with dataset


Functionality Included

1. Spam Prediction
   - Enter email features and get spam prediction
   - See confidence level of prediction
   - Works even with new feature combinations

2. Dataset Management
   - Add new emails to training data
   - Save changes to CSV file
   - Reset to original dataset

3. Accuracy Testing
   - Test classifier performance
   - View accuracy metrics
   - See false positives and negatives

4. User Interface
   - Prediction tab for checking emails
   - Add Data tab for training
   - Test Accuracy tab for performance
   - Simple controls to train and reset

Complete Frequency Table Dataset

The complete frequency table from `EmailSpamfrequency.csv`:

| Subject_Length | Urgent_Words | Sender_Domain | Attachments | Links | Not Spam | Spam | All |
|---------------|-------------|--------------|------------|-------|----------|------|-----|
| Long | No | Suspicious | No | Many | 0 | 5 | 5 |
| Long | No | Trusted | No | None | 14 | 0 | 14 |
| Long | No | Unknown | No | Few | 8 | 0 | 8 |
| Long | No | Unknown | No | None | 14 | 0 | 14 |
| Long | No | Unknown | Yes | Few | 1 | 0 | 1 |
| Long | Yes | Suspicious | No | Many | 0 | 15 | 15 |
| Long | Yes | Suspicious | Yes | Many | 0 | 6 | 6 |
| Long | Yes | Unknown | No | Many | 0 | 2 | 2 |
| Medium | No | Suspicious | No | Few | 0 | 1 | 1 |
| Medium | No | Trusted | No | Few | 23 | 0 | 23 |
| Medium | No | Trusted | Yes | Few | 9 | 0 | 9 |
| Medium | No | Unknown | No | Few | 20 | 0 | 20 |
| Medium | Yes | Suspicious | No | Many | 0 | 1 | 1 |
| Medium | Yes | Suspicious | Yes | Many | 0 | 14 | 14 |
| Short | No | Trusted | No | None | 29 | 0 | 29 |
| Short | Yes | Suspicious | No | Many | 0 | 22 | 22 |
| Short | Yes | Suspicious | Yes | Many | 0 | 10 | 10 |
| Short | Yes | Trusted | No | Few | 6 | 0 | 6 |
| All | | | | | 124 | 76 | 200 |

Dataset Summary
- Total emails: 200
- Non-spam emails: 124 (62%)
- Spam emails: 76 (38%)

Key Patterns in the Data
1. Spam indicators:
   - All emails from Suspicious domains are spam
   - Emails with Many links tend to be spam
   - Most emails with Urgent words are spam

2. Non-spam indicators:
   - Emails from Trusted domains are usually safe
   - Emails with No links are usually safe
   - Long emails without urgent words are generally safe

Project Structure
\`\`\`
EmailisSpamv2/
├── Email.java                    # Email class
├── SpamClassifier.java           # Classifier interface
├── FrequencyBasedSpamClassifier.java # Classifier implementation
├── DatasetManager.java           # Dataset handling
├── EmailSpamPredictorGUI.java    # Main GUI class
├── EmailSpamfrequency.csv        # Dataset file
└── README.md                     # This file
\`\`\`

What I Would Add With More Time

1. Better Classification Algorithms
   - Full Naive Bayes with prior probabilities
   - Support for continuous features
   - Decision Trees and Random Forests

2. Improved User Interface
   - Data visualizations and charts
   - Interactive confidence displays
   - Drag-and-drop for importing emails

3. Performance Upgrades
   - Optimizations for larger datasets
   - Caching frequent feature combinations
   - Multi-threading for training

4. Additional Features
   - More email features (time of day, size)
   - Export/import models
   - Integration with email clients
   - User accounts and personalized models

5. Enhanced Testing
   - Cross-validation
   - Visual confusion matrix
   - ROC curve and AUC calculation
