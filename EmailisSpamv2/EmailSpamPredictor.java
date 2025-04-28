import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Email Spam Predictor using Bayesian probability
 * This application predicts whether an email is spam or not based on its features
 * using a frequency-based approach with a training dataset.
 * 
 * Date: 24/04/2025
 * Author: Vengie Legaspi
 */
public class EmailSpamPredictor extends JFrame {
    // GUI Components
    private JComboBox<String> subjectLengthCombo, urgentWordsCombo, senderDomainCombo, attachmentsCombo, linksCombo;
    private JLabel resultLabel;
    private JProgressBar probabilityBar;
    private JTabbedPane tabbedPane;
    private JTextArea resultArea;

    // Data components
    private final Map<String, int[]> frequencyTable;  // Stores frequency counts [spam, notSpam] for each feature combination
    private int totalSpam = 0;     // Total spam examples in dataset
    private int totalNotSpam = 0;  // Total not-spam examples in dataset
    private final List<String[]> dataset;  // Full dataset as loaded from CSV or generated

    /**
     * Constructor: initializes the application UI and loads data
     */
    public EmailSpamPredictor() {
        super("Email Spam Predictor");
        initializeFrame();
        
        frequencyTable = new HashMap<>();
        dataset = new ArrayList<>();
        loadCSVData();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Predict", createPredictPanel());
        tabbedPane.addTab("Add Data", createAddDataPanel());
        tabbedPane.addTab("Accuracy", createAccuracyPanel());

        add(tabbedPane);
        setVisible(true);
    }

    /**
     * Sets up the main application window
     */
    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 450);
        setLocationRelativeTo(null);
    }

    // ==================== PREDICT PANEL ====================
    /**
     * Creates the panel for predicting spam based on input features
     */
    private JPanel createPredictPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form components
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        addFormComponents(form);

        // Result components
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        resultPanel.add(resultLabel, BorderLayout.NORTH);

        probabilityBar = new JProgressBar(0, 100);
        probabilityBar.setStringPainted(true);
        resultPanel.add(probabilityBar, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton trainBtn = new JButton("Train Classifier");
        trainBtn.addActionListener(e -> {
            loadCSVData();
            JOptionPane.showMessageDialog(this, "Classifier retrained with " + dataset.size() + " examples.");
        });
        buttonPanel.add(trainBtn);

        // Add all components to main panel
        panel.add(form, BorderLayout.NORTH);
        panel.add(resultPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Adds input components to the prediction form
     * @param form The form panel to add components to
     */
    private void addFormComponents(JPanel form) {
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
        predictBtn.addActionListener(this::predict);
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(predictBtn);
        form.add(buttonWrapper);
    }

    // ==================== ADD DATA PANEL ====================
    /**
     * Creates the panel for adding new data to the dataset
     * @return JPanel containing the data entry interface
     */
    private JPanel createAddDataPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        addDataFormComponents(form);

        panel.add(form, BorderLayout.NORTH);
        return panel;
    }

    /**
     * Adds input components to the data entry form
     * @param form The form panel to add components to
     */
    private void addDataFormComponents(JPanel form) {
        JComboBox<String> sLength = new JComboBox<>(new String[]{"Short", "Medium", "Long"});
        JComboBox<String> urgent = new JComboBox<>(new String[]{"No", "Yes"});
        JComboBox<String> domain = new JComboBox<>(new String[]{"Trusted", "Unknown", "Suspicious"});
        JComboBox<String> attach = new JComboBox<>(new String[]{"No", "Yes"});
        JComboBox<String> links = new JComboBox<>(new String[]{"None", "Few", "Many"});
        JComboBox<String> isSpam = new JComboBox<>(new String[]{"No", "Yes"});

        form.add(new JLabel("Subject Length:")); form.add(sLength);
        form.add(new JLabel("Urgent Words:")); form.add(urgent);
        form.add(new JLabel("Sender Domain:")); form.add(domain);
        form.add(new JLabel("Attachments:")); form.add(attach);
        form.add(new JLabel("Links:")); form.add(links);
        form.add(new JLabel("Is Spam:")); form.add(isSpam);

        form.add(new JLabel(""));
        JButton addBtn = new JButton("Add Data");
        addBtn.addActionListener(e -> {
            String[] entry = { 
                sLength.getSelectedItem().toString(), 
                urgent.getSelectedItem().toString(),
                domain.getSelectedItem().toString(), 
                attach.getSelectedItem().toString(),
                links.getSelectedItem().toString(), 
                isSpam.getSelectedItem().toString() 
            };
            addData(entry);
            JOptionPane.showMessageDialog(this, "Data added successfully!\nTotal records in dataset: " + dataset.size());
        });
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(addBtn);
        form.add(buttonWrapper);
    }

    // ==================== ACCURACY PANEL ====================
    /**
     * Creates the panel for testing the model's accuracy
     * @return JPanel containing the accuracy testing interface
     */
    private JPanel createAccuracyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton testButton = new JButton("Test Accuracy (150 train / 50 test)");
        testButton.addActionListener(e -> testAccuracy());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(testButton);
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        return panel;
    }

    // ==================== PREDICTION LOGIC ====================
    /**
     * Performs spam prediction based on selected feature values
     * @param e Action event from the predict button
     */
    private void predict(ActionEvent e) {
        String features = String.join(",",
            subjectLengthCombo.getSelectedItem().toString(),
            urgentWordsCombo.getSelectedItem().toString(),
            senderDomainCombo.getSelectedItem().toString(),
            attachmentsCombo.getSelectedItem().toString(),
            linksCombo.getSelectedItem().toString()
        );
        
        if (!frequencyTable.containsKey(features)) {
            resultLabel.setText("Combination not found in dataset");
            probabilityBar.setValue(0);
            return;
        }
        
        int[] counts = frequencyTable.get(features);
        double probability = (counts[0] * 100.0) / (counts[0] + counts[1]);
        
        probabilityBar.setValue((int) probability);
        if (probability > 50) {
            resultLabel.setText(String.format("Spam (%.1f%%)", probability));
            resultLabel.setForeground(Color.RED);
        } else {
            resultLabel.setText(String.format("Not Spam (%.1f%%)", 100 - probability));
            resultLabel.setForeground(new Color(0, 128, 0));
        }
    }

    // ==================== DATA MANAGEMENT ====================
    /**
     * Adds a new data record to the dataset and updates frequency counts
     * @param record Array of feature values and class label
     */
    private void addData(String[] record) {
        String features = String.join(",", record[0], record[1], record[2], record[3], record[4]);
        boolean isSpam = record[5].equals("Yes");
        
        frequencyTable.putIfAbsent(features, new int[2]);
        if (isSpam) {
            frequencyTable.get(features)[0]++;
            totalSpam++;
        } else {
            frequencyTable.get(features)[1]++;
            totalNotSpam++;
        }
        
        dataset.add(record);
    }

    /**
     * Tests the accuracy of the model using stratified sampling
     * Ensures that training and testing sets have the same proportion of spam/not spam
     */
    private void testAccuracy() {
        if (dataset.size() < 200) {
            JOptionPane.showMessageDialog(this, 
                "Need at least 200 examples for accuracy test (have " + dataset.size() + ")");
            return;
        }
        
        // Separate data into spam and not spam for stratified sampling
        List<String[]> spamData = new ArrayList<>();
        List<String[]> notSpamData = new ArrayList<>();
        
        for (String[] record : dataset) {
            if (record[5].equals("Yes")) {
                spamData.add(record);
            } else {
                notSpamData.add(record);
            }
        }
        
        // Calculate proportions for stratification
        int totalExamples = dataset.size();
        double spamProportion = spamData.size() / (double) totalExamples;
        
        // Calculate how many examples of each class should be in training set (maintaining proportion)
        int trainingSize = 150;
        int testingSize = 50;
        int spamTrainingCount = (int) (trainingSize * spamProportion);
        int notSpamTrainingCount = trainingSize - spamTrainingCount;
        
        // Shuffle individual lists to ensure randomness within each class
        Collections.shuffle(spamData);
        Collections.shuffle(notSpamData);
        
        // Create stratified training and testing sets
        List<String[]> trainingData = new ArrayList<>();
        List<String[]> testingData = new ArrayList<>();
        
        // Add spam examples to training set
        for (int i = 0; i < spamTrainingCount; i++) {
            trainingData.add(spamData.get(i));
        }
        
        // Add not-spam examples to training set
        for (int i = 0; i < notSpamTrainingCount; i++) {
            trainingData.add(notSpamData.get(i));
        }
        
        // Add remaining spam examples to testing set
        for (int i = spamTrainingCount; i < spamData.size(); i++) {
            testingData.add(spamData.get(i));
        }
        
        // Add remaining not-spam examples to testing set
        for (int i = notSpamTrainingCount; i < notSpamData.size(); i++) {
            testingData.add(notSpamData.get(i));
        }
        
        // Re-shuffle training and testing sets to avoid having all spam first
        Collections.shuffle(trainingData);
        Collections.shuffle(testingData);
        
        // Train on training data
        Map<String, int[]> tempFrequencyTable = new HashMap<>();
        for (String[] record : trainingData) {
            String features = String.join(",", record[0], record[1], record[2], record[3], record[4]);
            boolean isSpam = record[5].equals("Yes");
            
            tempFrequencyTable.putIfAbsent(features, new int[2]);
            if (isSpam) {
                tempFrequencyTable.get(features)[0]++;
            } else {
                tempFrequencyTable.get(features)[1]++;
            }
        }
        
        // Test on testing data
        int correct = 0;
        int truePositives = 0;
        int falsePositives = 0;
        int trueNegatives = 0;
        int falseNegatives = 0;
        StringBuilder results = new StringBuilder();
        
        for (String[] record : testingData) {
            String features = String.join(",", record[0], record[1], record[2], record[3], record[4]);
            boolean actualIsSpam = record[5].equals("Yes");
            
            boolean predictedIsSpam = false;
            double probability = 0.0;
            
            if (tempFrequencyTable.containsKey(features)) {
                int[] counts = tempFrequencyTable.get(features);
                probability = (counts[0]) / (double) (counts[0] + counts[1]);
                predictedIsSpam = probability > 0.5;
            }
            
            // Update confusion matrix
            if (predictedIsSpam && actualIsSpam) truePositives++;
            if (predictedIsSpam && !actualIsSpam) falsePositives++;
            if (!predictedIsSpam && !actualIsSpam) trueNegatives++;
            if (!predictedIsSpam && actualIsSpam) falseNegatives++;
            
            if (predictedIsSpam == actualIsSpam) {
                correct++;
            }
            
            results.append(String.format("Features: %s | Actual: %s | Predicted: %s | Probability: %.2f\n", 
                features, actualIsSpam ? "Spam" : "Not Spam", predictedIsSpam ? "Spam" : "Not Spam", probability));
        }
        
        // Calculate performance metrics
        double accuracy = (correct * 100.0) / testingData.size();
        double precision = truePositives > 0 ? (truePositives * 100.0) / (truePositives + falsePositives) : 0;
        double recall = truePositives > 0 ? (truePositives * 100.0) / (truePositives + falseNegatives) : 0;
        
        // Display stratification information
        int trainSpamCount = 0;
        for (String[] record : trainingData) {
            if (record[5].equals("Yes")) trainSpamCount++;
        }
        
        int testSpamCount = 0;
        for (String[] record : testingData) {
            if (record[5].equals("Yes")) testSpamCount++;
        }
        
        // Prepare the results output
        results.insert(0, String.format(
            "Stratified Sampling Information:\n" +
            "  - Training set: %d examples (%d spam, %.1f%%)\n" +
            "  - Testing set: %d examples (%d spam, %.1f%%)\n\n" +
            "Accuracy: %.1f%% (%d correct out of %d)\n" +
            "Precision: %.1f%%\n" +
            "Recall: %.1f%%\n" +
            "True Positives: %d | False Positives: %d\n" +
            "True Negatives: %d | False Negatives: %d\n\n",
            trainingData.size(), trainSpamCount, (trainSpamCount * 100.0) / trainingData.size(),
            testingData.size(), testSpamCount, (testSpamCount * 100.0) / testingData.size(),
            accuracy, correct, testingData.size(), precision, recall,
            truePositives, falsePositives, trueNegatives, falseNegatives));
        
        resultArea.setText(results.toString());
    }

    /**
     * Loads email data from CSV file or generates sample data if file not found
     */
    private void loadCSVData() {
        frequencyTable.clear();
        dataset.clear();
        totalSpam = 0;
        totalNotSpam = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("EmailSpamfrequency.csv"))) {
            String line;
            boolean headerSkipped = false;
            
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                
                // Skip the total row
                if (line.startsWith("All")) {
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 8) { // Make sure we have enough columns
                    String subjectLength = values[0].trim();
                    String urgentWords = values[1].trim();
                    String senderDomain = values[2].trim();
                    String attachments = values[3].trim();
                    String links = values[4].trim();
                    
                    // Check if we have valid counts in the last columns
                    int notSpamCount = 0;
                    int spamCount = 0;
                    
                    try {
                        notSpamCount = Integer.parseInt(values[5].trim());
                        spamCount = Integer.parseInt(values[6].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid row: " + line);
                        continue;
                    }
                    
                    String key = String.join(",", subjectLength, urgentWords, senderDomain, attachments, links);
                    frequencyTable.put(key, new int[]{spamCount, notSpamCount});
                    
                    totalSpam += spamCount;
                    totalNotSpam += notSpamCount;
                    
                    // Add individual records to dataset for accuracy testing
                    for (int i = 0; i < spamCount; i++) {
                        dataset.add(new String[]{subjectLength, urgentWords, senderDomain, attachments, links, "Yes"});
                    }
                    for (int i = 0; i < notSpamCount; i++) {
                        dataset.add(new String[]{subjectLength, urgentWords, senderDomain, attachments, links, "No"});
                    }
                }
            }
            System.out.println("Loaded " + dataset.size() + " records from CSV");
            
            if (dataset.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid data found in CSV file. Generating sample data.", 
                                            "Warning", JOptionPane.WARNING_MESSAGE);
                generateSampleData();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading CSV data: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            generateSampleData();
        }
    }

    /**
     * Generates sample data if CSV file cannot be loaded
     * Creates examples for all possible feature combinations
     */
    private void generateSampleData() {
        String[] lengths = {"Short", "Medium", "Long"};
        String[] urgent = {"No", "Yes"};
        String[] domains = {"Trusted", "Unknown", "Suspicious"};
        String[] attachments = {"No", "Yes"};
        String[] links = {"None", "Few", "Many"};
        
        for (String len : lengths) {
            for (String urg : urgent) {
                for (String dom : domains) {
                    for (String att : attachments) {
                        for (String lnk : links) {
                            int spamCount = 0;
                            int notSpamCount = 0;
                            
                            // Generate logical sample data
                            if (dom.equals("Suspicious") && urg.equals("Yes") && lnk.equals("Many")) {
                                spamCount = 20;
                                notSpamCount = 1;
                            } else if (dom.equals("Suspicious") || (urg.equals("Yes") && lnk.equals("Many"))) {
                                spamCount = 5;
                                notSpamCount = 2;
                            } else if (urg.equals("Yes") || lnk.equals("Many")) {
                                spamCount = 3;
                                notSpamCount = 4;
                            } else {
                                spamCount = 1;
                                notSpamCount = 10;
                            }
                            
                            String key = String.join(",", len, urg, dom, att, lnk);
                            frequencyTable.put(key, new int[]{spamCount, notSpamCount});
                            
                            totalSpam += spamCount;
                            totalNotSpam += notSpamCount;
                            
                            for (int i = 0; i < spamCount; i++) {
                                dataset.add(new String[]{len, urg, dom, att, lnk, "Yes"});
                            }
                            for (int i = 0; i < notSpamCount; i++) {
                                dataset.add(new String[]{len, urg, dom, att, lnk, "No"});
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmailSpamPredictor());
    }
}