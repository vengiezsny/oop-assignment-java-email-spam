import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
* Main GUI class for the Email Spam Predictor application
* Handles user interaction and coordinates between the classifier and dataset manager
* 
* This project is for learning about basic machine learning, probability, and how computers can make predictions from data.
*
* Assignment: OOP Sem 2 2025
* Date: 29/04/2025
* Author: Vengie Legaspi
**/
public class EmailSpamPredictorGUI extends JFrame 
{
    // GUI Components
    private JComboBox<String> subjectLengthCombo, urgentWordsCombo, senderDomainCombo, attachmentsCombo, linksCombo;
    private JLabel resultLabel;
    private JTabbedPane tabbedPane;
    private JTextArea resultArea;
    
    // Data components
    private final DatasetManager datasetManager;
    private final SpamClassifier classifier;
    
    /**
     * Constructor - initializes the GUI and loads data
     */
    public EmailSpamPredictorGUI() {
        super("Email Spam Predictor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Initialize data components
        datasetManager = new DatasetManager();
        classifier = new FrequencyBasedSpamClassifier();
        
        // Load data from CSV
        datasetManager.loadFromCSV("EmailSpamfrequency.csv");
        classifier.train(datasetManager.getDataset());
        
        // Create GUI
        createGUI();
        
        setVisible(true);
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default look and feel
        }
        
        SwingUtilities.invokeLater(EmailSpamPredictorGUI::new);
    }
    
    /**
     * Creates the main GUI components
     */
    private void createGUI() {
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Predict", createPredictPanel());
        tabbedPane.addTab("Add Data", createAddDataPanel());
        tabbedPane.addTab("Test Accuracy", createTestPanel());
        
        add(tabbedPane);
    }
    
    /**
     * Creates the panel for predicting spam
     */
    private JPanel createPredictPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Form for entering email features
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 10));
        addPredictFormComponents(form);
        panel.add(form, BorderLayout.CENTER);
        
        // Result label
        resultLabel = new JLabel("Enter email features and click Predict");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Add Train Classifier button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton trainBtn = new JButton("Train Classifier");
        trainBtn.addActionListener(__ -> {
            datasetManager.loadFromCSV("EmailSpamfrequency.csv");
            classifier.train(datasetManager.getDataset());
            JOptionPane.showMessageDialog(this, "Classifier retrained with " + 
                                         datasetManager.getDataset().size() + " examples.");
        });
        buttonPanel.add(trainBtn);
        
        // Add Reset Data button
        JButton resetBtn = new JButton("Reset Data");
        resetBtn.addActionListener(__ -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "This will reset the dataset to its original state.\nAny added data will be lost.\nContinue?", 
                "Reset Dataset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                datasetManager.resetToOriginalData("EmailSpamfrequency.csv");
                classifier.train(datasetManager.getDataset());
                JOptionPane.showMessageDialog(this, "Dataset reset to original state with " + 
                                             datasetManager.getDataset().size() + " examples.");
            }
        });
        buttonPanel.add(resetBtn);
        
        // Create a panel for the bottom section
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(resultLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Adds components to the prediction form
     */
    private void addPredictFormComponents(JPanel form) {
        form.add(new JLabel("Subject Length:"));
        subjectLengthCombo = new JComboBox<>(new String[]{"Short", "Medium", "Long"});
        form.add(subjectLengthCombo);
        
        form.add(new JLabel("Urgent Words:"));
        urgentWordsCombo = new JComboBox<>(new String[]{"No", "Yes"});
        form.add(urgentWordsCombo);
        
        form.add(new JLabel("Sender Domain:"));
        senderDomainCombo = new JComboBox<>(new String[]{"Trusted", "Unknown", "Suspicious"});
        form.add(senderDomainCombo);
        
        form.add(new JLabel("Attachments:"));
        attachmentsCombo = new JComboBox<>(new String[]{"No", "Yes"});
        form.add(attachmentsCombo);
        
        form.add(new JLabel("Links:"));
        linksCombo = new JComboBox<>(new String[]{"None", "Few", "Many"});
        form.add(linksCombo);
        
        form.add(new JLabel(""));
        JButton predictBtn = new JButton("Predict");
        predictBtn.addActionListener(__ -> predict());
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(predictBtn);
        form.add(buttonWrapper);
    }

    /**
     * Performs spam prediction based on selected feature values
     */
    private void predict() {
        Email email = new Email(
                (String) subjectLengthCombo.getSelectedItem(),
                (String) urgentWordsCombo.getSelectedItem(),
                (String) senderDomainCombo.getSelectedItem(),
                (String) attachmentsCombo.getSelectedItem(),
                (String) linksCombo.getSelectedItem(),
                false // label not needed for prediction
        );
        
        double prob = classifier.predictProbability(email);
        
        if (prob == -1) {
            resultLabel.setText("Combination not found in dataset");
            return;
        }
        
        if (prob > 0.5) {
            resultLabel.setText(String.format("Spam (%.1f%%)", prob * 100));
            resultLabel.setForeground(Color.RED);
        } else {
            resultLabel.setText(String.format("Not Spam (%.1f%%)", (1 - prob) * 100));
            resultLabel.setForeground(new Color(0, 128, 0));
        }
    }

    /**
     * Creates the panel for adding new data to the dataset
     */
    private JPanel createAddDataPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Form for entering email features
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 10));
        addDataFormComponents(form);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Adds components to the add data form
     */
    private void addDataFormComponents(JPanel form) {
        JComboBox<String> subj = new JComboBox<>(new String[]{"Short", "Medium", "Long"});
        JComboBox<String> urg = new JComboBox<>(new String[]{"No", "Yes"});
        JComboBox<String> dom = new JComboBox<>(new String[]{"Trusted", "Unknown", "Suspicious"});
        JComboBox<String> att = new JComboBox<>(new String[]{"No", "Yes"});
        JComboBox<String> lnk = new JComboBox<>(new String[]{"None", "Few", "Many"});
        JComboBox<String> spam = new JComboBox<>(new String[]{"No", "Yes"});
        
        form.add(new JLabel("Subject Length:"));
        form.add(subj);
        
        form.add(new JLabel("Urgent Words:"));
        form.add(urg);
        
        form.add(new JLabel("Sender Domain:"));
        form.add(dom);
        
        form.add(new JLabel("Attachments:"));
        form.add(att);
        
        form.add(new JLabel("Links:"));
        form.add(lnk);
        
        form.add(new JLabel("Is Spam:"));
        form.add(spam);
        
        form.add(new JLabel(""));
        JButton addBtn = new JButton("Add Data");
        addBtn.addActionListener(__ -> {
            Email email = new Email(
                    (String) subj.getSelectedItem(),
                    (String) urg.getSelectedItem(),
                    (String) dom.getSelectedItem(),
                    (String) att.getSelectedItem(),
                    (String) lnk.getSelectedItem(),
                    spam.getSelectedItem().equals("Yes")
            );
            
            datasetManager.addEmail(email);
            classifier.train(datasetManager.getDataset());
            datasetManager.saveToCSV("EmailSpamfrequency.csv");
            
            JOptionPane.showMessageDialog(this, 
                "Data added successfully!\nTotal records in dataset: " + 
                datasetManager.getDataset().size());
        });
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(addBtn);
        form.add(buttonWrapper);
    }

    /**
     * Creates the panel for testing the model's accuracy
     */
    private JPanel createTestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton testBtn = new JButton("Test Accuracy (150 train / 50 test)");
        testBtn.addActionListener(__ -> testAccuracy());
        testBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(testBtn);
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tests the accuracy of the classifier using stratified sampling
     * Ensures that training and testing sets have the same proportion of spam/not spam
     */
    private void testAccuracy() {
        List<Email> all = datasetManager.getDataset();
        if (all.size() < 200) {
            resultArea.setText("Need at least 200 examples for accuracy test (have " + all.size() + ")");
            return;
        }
        
        // Separate spam and non-spam emails
        List<Email> spamEmails = new ArrayList<>();
        List<Email> notSpamEmails = new ArrayList<>();
        
        for (Email email : all) {
            if (email.isSpam()) {
                spamEmails.add(email);
            } else {
                notSpamEmails.add(email);
            }
        }
        
        // Shuffle both lists
        Collections.shuffle(spamEmails);
        Collections.shuffle(notSpamEmails);
        
        // Calculate proportions for stratified sampling
        int totalSize = all.size();
        int spamSize = spamEmails.size();
        // Removed unused variable: int notSpamSize = notSpamEmails.size();
        
        double spamRatio = spamSize / (double) totalSize;
        
        // Create training and testing sets with stratified sampling
        int trainSize = 150;
        int testSize = 50;
        
        int trainSpamCount = (int) Math.round(trainSize * spamRatio);
        int trainNotSpamCount = trainSize - trainSpamCount;
        
        int testSpamCount = (int) Math.round(testSize * spamRatio);
        int testNotSpamCount = testSize - testSpamCount;
        
        // Create training and testing sets
        List<Email> trainingData = new ArrayList<>();
        List<Email> testingData = new ArrayList<>();
        
        // Add spam emails
        for (int i = 0; i < trainSpamCount && i < spamEmails.size(); i++) {
            trainingData.add(spamEmails.get(i));
        }
        for (int i = trainSpamCount; i < trainSpamCount + testSpamCount && i < spamEmails.size(); i++) {
            testingData.add(spamEmails.get(i));
        }
        
        // Add not-spam emails
        for (int i = 0; i < trainNotSpamCount && i < notSpamEmails.size(); i++) {
            trainingData.add(notSpamEmails.get(i));
        }
        for (int i = trainNotSpamCount; i < trainNotSpamCount + testNotSpamCount && i < notSpamEmails.size(); i++) {
            testingData.add(notSpamEmails.get(i));
        }
        
        // Shuffle training and testing sets
        Collections.shuffle(trainingData);
        Collections.shuffle(testingData);
        
        // Train on training data
        SpamClassifier testClassifier = new FrequencyBasedSpamClassifier();
        testClassifier.train(trainingData);
        
        // Test on testing data
        int correct = 0, truePos = 0, falsePos = 0, trueNeg = 0, falseNeg = 0;
        
        for (Email email : testingData) {
            boolean actual = email.isSpam();
            boolean predicted = testClassifier.predict(email);
            
            if (predicted == actual) correct++;
            if (predicted && actual) truePos++;
            if (predicted && !actual) falsePos++;
            if (!predicted && !actual) trueNeg++;
            if (!predicted && actual) falseNeg++;
        }
        
        double accuracy = (correct * 100.0) / testingData.size();
        double precision = truePos > 0 ? (truePos * 100.0) / (truePos + falsePos) : 0;
        double recall = truePos > 0 ? (truePos * 100.0) / (truePos + falseNeg) : 0;
        
        // Count spam in training and testing sets
        int trainSpam = 0, trainNotSpam = 0, testSpam = 0, testNotSpam = 0;
        for (Email email : trainingData) {
            if (email.isSpam()) trainSpam++;
            else trainNotSpam++;
        }
        for (Email email : testingData) {
            if (email.isSpam()) testSpam++;
            else testNotSpam++;
        }
        
        // Format and display results
        resultArea.setText(String.format(
            "Accuracy Test Results:\n\n" +
            "Training Set (150 emails):\n" +
            "  Spam: %d (%.1f%%)\n" +
            "  Not Spam: %d (%.1f%%)\n\n" +
            "Testing Set (50 emails):\n" +
            "  Spam: %d (%.1f%%)\n" +
            "  Not Spam: %d (%.1f%%)\n\n" +
            "Performance Metrics:\n" +
            "  Accuracy: %.1f%%\n" +
            "  Precision: %.1f%%\n" +
            "  Recall: %.1f%%\n\n" +
            "Confusion Matrix:\n" +
            "  True Positives: %d\n" +
            "  False Positives: %d\n" +
            "  True Negatives: %d\n" +
            "  False Negatives: %d",
            
            trainSpam, (trainSpam * 100.0) / trainingData.size(),
            trainNotSpam, (trainNotSpam * 100.0) / trainingData.size(),
            testSpam, (testSpam * 100.0) / testingData.size(),
            testNotSpam, (testNotSpam * 100.0) / testingData.size(),
            accuracy, precision, recall,
            truePos, falsePos, trueNeg, falseNeg
        ));
    }
}
