import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import javax.swing.JOptionPane;

/**
 * Handles loading, saving, and managing the dataset of emails
 * 
 * This class is responsible for all data-related operations:
 * 
 * 1. Loading emails from a CSV file
 *    - Reads the frequency-based format with columns for spam and not-spam counts
 *    - Creates individual Email objects for each entry
 * 
 * 2. Saving emails to a CSV file
 *    - Converts the dataset back to frequency format
 *    - Maintains the same CSV structure with totals row
 * 
 * 3. Managing the dataset
 *    - Adding new emails
 *    - Generating sample data if needed
 *    - Resetting to original data
 * 
 * This class acts as a "Data Access Object" (DAO) that separates data 
 * storage logic from the rest of the application.
 */
public class DatasetManager {
    // Constants for CSV file structure
    private static final int SUBJECT_LENGTH_INDEX = 0;
    private static final int URGENT_WORDS_INDEX = 1;
    private static final int SENDER_DOMAIN_INDEX = 2;
    private static final int ATTACHMENTS_INDEX = 3;
    private static final int LINKS_INDEX = 4;
    private static final int NOT_SPAM_COUNT_INDEX = 5;
    private static final int SPAM_COUNT_INDEX = 6;
    
    // Data storage
    private final List<Email> dataset = new ArrayList<>();
    private final Map<String, int[]> frequencyTable = new HashMap<>();
    
    // These counters are kept for potential future enhancements
    // For example, they could be used for reporting or advanced analytics
    @SuppressWarnings("unused")
    private int totalSpam = 0;
    @SuppressWarnings("unused")
    private int totalNotSpam = 0;

    /**
     * Returns the current dataset
     * @return List of Email objects
     */
    public List<Email> getDataset() {
        return dataset;
    }

    /**
     * Loads emails from a CSV file using frequency format
     * @param filename Path to the CSV file
     */
    public void loadFromCSV(String filename) {
        // Clear existing data
        dataset.clear();
        frequencyTable.clear();
        totalSpam = 0;
        totalNotSpam = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean headerSkipped = false;
            
            while ((line = reader.readLine()) != null) {
                // Skip the header row
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
                    // Extract feature values
                    String subjectLength = values[SUBJECT_LENGTH_INDEX].trim();
                    String urgentWords = values[URGENT_WORDS_INDEX].trim();
                    String senderDomain = values[SENDER_DOMAIN_INDEX].trim();
                    String attachments = values[ATTACHMENTS_INDEX].trim();
                    String links = values[LINKS_INDEX].trim();
                    
                    // Extract counts
                    int notSpamCount = 0;
                    int spamCount = 0;
                    
                    try {
                        notSpamCount = Integer.parseInt(values[NOT_SPAM_COUNT_INDEX].trim());
                        spamCount = Integer.parseInt(values[SPAM_COUNT_INDEX].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid row: " + line);
                        continue;
                    }
                    
                    // Create a key for the frequency table
                    String key = String.join(",", subjectLength, urgentWords, senderDomain, attachments, links);
                    frequencyTable.put(key, new int[]{spamCount, notSpamCount});
                    
                    // Update totals
                    totalSpam += spamCount;
                    totalNotSpam += notSpamCount;
                    
                    // Add individual records to dataset for accuracy testing
                    // For each spam count, add that many spam emails with these features
                    for (int i = 0; i < spamCount; i++) {
                        dataset.add(new Email(subjectLength, urgentWords, senderDomain, attachments, links, true));
                    }
                    
                    // For each not-spam count, add that many not-spam emails with these features
                    for (int i = 0; i < notSpamCount; i++) {
                        dataset.add(new Email(subjectLength, urgentWords, senderDomain, attachments, links, false));
                    }
                }
            }
            System.out.println("Loaded " + dataset.size() + " records from CSV");
            
            // If no valid data was found, generate sample data
            if (dataset.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No valid data found in CSV file. Generating sample data.", 
                        "No Data Found", JOptionPane.WARNING_MESSAGE);
                generateSampleData();
            }
        } catch (IOException e) {
            System.out.println("Error loading CSV: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error loading CSV file: " + e.getMessage() + ". Generating sample data.", 
                    "Error Loading Data", JOptionPane.ERROR_MESSAGE);
            generateSampleData();
        }
    }

    /**
     * Saves emails to a CSV file in frequency format
     * @param filename Path to the CSV file
     */
    public void saveToCSV(String filename) {
        // Build frequency table from current dataset
        Map<String, int[]> currentFrequencyTable = new HashMap<>();
        
        // Count occurrences of each feature combination
        for (Email email : dataset) {
            String key = String.join(",", 
                email.getSubjectLength(),
                email.getUrgentWords(),
                email.getSenderDomain(),
                email.getAttachments(),
                email.getLinks()
            );
            
            // Initialize counts if this is a new combination
            currentFrequencyTable.putIfAbsent(key, new int[]{0, 0});
            
            // Increment appropriate counter
            if (email.isSpam()) {
                currentFrequencyTable.get(key)[0]++;
            } else {
                currentFrequencyTable.get(key)[1]++;
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("Subject_Length,Urgent_Words,Sender_Domain,Attachments,Links,Not Spam,Spam,All");
            
            // Calculate totals
            int totalSpamCount = 0;
            int totalNotSpamCount = 0;
            
            // Write data rows
            for (Map.Entry<String, int[]> entry : currentFrequencyTable.entrySet()) {
                String key = entry.getKey();
                int spamCount = entry.getValue()[0];
                int notSpamCount = entry.getValue()[1];
                int total = spamCount + notSpamCount;
                
                totalSpamCount += spamCount;
                totalNotSpamCount += notSpamCount;
                
                writer.println(key + "," + notSpamCount + "," + spamCount + "," + total);
            }
            
            // Write totals row
            int grandTotal = totalNotSpamCount + totalSpamCount;
            writer.println("All,,,,,," + totalNotSpamCount + "," + totalSpamCount + "," + grandTotal);
            
            System.out.println("Saved " + dataset.size() + " records to CSV");
        } catch (IOException e) {
            System.out.println("Error saving CSV: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error saving CSV file: " + e.getMessage(), 
                    "Error Saving Data", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds a new email to the dataset
     * @param email Email object to add
     */
    public void addEmail(Email email) {
        dataset.add(email);
    }

    /**
     * Resets the dataset to the original state and saves it to CSV
     * @param filename Path to the CSV file to save the reset data
     */
    public void resetToOriginalData(String filename) {
        // Clear existing data
        dataset.clear();
        frequencyTable.clear();
        totalSpam = 0;
        totalNotSpam = 0;
        
        // Create the original dataset with exactly 200 examples
        // Original data with exact counts to match the original CSV
        addExactEmail("Long", "No", "Unknown", "Yes", "Few", false, 1);
        addExactEmail("Medium", "No", "Trusted", "No", "Few", false, 23);
        addExactEmail("Medium", "No", "Trusted", "Yes", "Few", false, 9);
        addExactEmail("Medium", "Yes", "Suspicious", "Yes", "Many", true, 14);
        addExactEmail("Medium", "No", "Unknown", "No", "Few", false, 20);
        addExactEmail("Long", "No", "Unknown", "No", "Few", false, 8);
        addExactEmail("Medium", "No", "Suspicious", "No", "Few", true, 1);
        addExactEmail("Short", "Yes", "Trusted", "No", "Few", false, 6);
        addExactEmail("Long", "Yes", "Suspicious", "Yes", "Many", true, 6);
        addExactEmail("Long", "No", "Unknown", "No", "None", false, 14);
        addExactEmail("Short", "Yes", "Suspicious", "Yes", "Many", true, 10);
        addExactEmail("Long", "No", "Trusted", "No", "None", false, 14);
        addExactEmail("Long", "Yes", "Unknown", "No", "Many", true, 2);
        addExactEmail("Short", "Yes", "Suspicious", "No", "Many", true, 22);
        addExactEmail("Medium", "Yes", "Suspicious", "No", "Many", true, 1);
        addExactEmail("Short", "No", "Trusted", "No", "None", false, 29);
        addExactEmail("Long", "No", "Suspicious", "No", "Many", true, 5);
        addExactEmail("Long", "Yes", "Suspicious", "No", "Many", true, 15);
        
        // Save the reset data to CSV
        saveToCSV(filename);
        
        System.out.println("Dataset reset to original state with " + dataset.size() + " records");
    }
    
    /**
     * Generates sample data if CSV file cannot be loaded or is empty
     */
    private void generateSampleData() {
        System.out.println("Generating sample data...");
        dataset.clear();
        
        // Add sample data with various combinations
        addMultipleEmails("Long", "No", "Unknown", "Yes", "Few", false, 1);
        addMultipleEmails("Medium", "No", "Trusted", "No", "Few", false, 23);
        addMultipleEmails("Medium", "No", "Trusted", "Yes", "Few", false, 9);
        addMultipleEmails("Medium", "Yes", "Suspicious", "Yes", "Many", true, 14);
        addMultipleEmails("Medium", "No", "Unknown", "No", "Few", false, 20);
        addMultipleEmails("Long", "No", "Unknown", "No", "Few", false, 8);
        addMultipleEmails("Medium", "No", "Suspicious", "No", "Few", true, 1);
        addMultipleEmails("Short", "Yes", "Trusted", "No", "Few", false, 6);
        addMultipleEmails("Long", "Yes", "Suspicious", "Yes", "Many", true, 6);
        addMultipleEmails("Long", "No", "Unknown", "No", "None", false, 14);
        addMultipleEmails("Short", "Yes", "Suspicious", "Yes", "Many", true, 10);
        addMultipleEmails("Long", "No", "Trusted", "No", "None", false, 14);
        addMultipleEmails("Long", "Yes", "Unknown", "No", "Many", true, 2);
        addMultipleEmails("Short", "Yes", "Suspicious", "No", "Many", true, 22);
        addMultipleEmails("Medium", "Yes", "Suspicious", "No", "Many", true, 1);
        addMultipleEmails("Short", "No", "Trusted", "No", "None", false, 29);
        addMultipleEmails("Long", "No", "Suspicious", "No", "Many", true, 5);
        addMultipleEmails("Long", "Yes", "Suspicious", "No", "Many", true, 15);
        
        System.out.println("Generated " + dataset.size() + " sample records");
    }
    
    /**
     * Adds exact number of emails with the specified features to the dataset
     * Used for resetting to original data with precise counts
     * 
     * @param subjectLength Length category of the email subject
     * @param urgentWords Whether the email contains urgent words
     * @param senderDomain Category of the sender's domain
     * @param attachments Whether the email has attachments
     * @param links Category for the number of links in the email
     * @param isSpam Whether this email is classified as spam
     * @param count Number of emails to add with these features
     */
    private void addExactEmail(String subjectLength, String urgentWords, String senderDomain, 
                             String attachments, String links, boolean isSpam, int count) {
        // Create a key for the frequency table
        String key = String.join(",", subjectLength, urgentWords, senderDomain, attachments, links);
        
        // Initialize counts if this is a new combination
        frequencyTable.putIfAbsent(key, new int[]{0, 0});
        
        // Update counters
        if (isSpam) {
            frequencyTable.get(key)[0] += count;
            totalSpam += count;
        } else {
            frequencyTable.get(key)[1] += count;
            totalNotSpam += count;
        }
        
        // Add individual records to dataset
        for (int i = 0; i < count; i++) {
            dataset.add(new Email(subjectLength, urgentWords, senderDomain, attachments, links, isSpam));
        }
    }
    
    /**
     * Adds multiple emails with the same features to the dataset
     * Used for generating sample data
     * 
     * @param subjectLength Length category of the email subject
     * @param urgentWords Whether the email contains urgent words
     * @param senderDomain Category of the sender's domain
     * @param attachments Whether the email has attachments
     * @param links Category for the number of links in the email
     * @param isSpam Whether this email is classified as spam
     * @param count Number of emails to add with these features
     */
    private void addMultipleEmails(String subjectLength, String urgentWords, String senderDomain, 
                                 String attachments, String links, boolean isSpam, int count) {
        for (int i = 0; i < count; i++) {
            dataset.add(new Email(subjectLength, urgentWords, senderDomain, attachments, links, isSpam));
        }
    }
}
