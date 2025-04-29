import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Implements SpamClassifier using a frequency-based approach
 * 
 * This class uses a simple but effective method to predict if an email is spam:
 * 1. It counts how many times each combination of features appears in spam and non-spam emails
 * 2. When predicting, it looks up the counts for that combination
 * 3. It calculates probability as: (spam count) / (spam count + not spam count)
 * 
 * For example, if emails with "Short" subject, "Yes" urgent words, and "Suspicious" domain
 * appear 20 times as spam and 5 times as not spam, then the probability would be 20/(20+5) = 0.8 or 80%.
 * 
 * If a combination isn't found in our data, it returns -1 to indicate "not found."
 * 
 * This is similar to a simplified Naive Bayes classifier, a common technique in spam filtering.
 */
public class FrequencyBasedSpamClassifier implements SpamClassifier {
    // Map to store frequency counts: [key: feature combination] -> [value: [spam count, not-spam count]]
    private final Map<String, int[]> frequencyTable = new HashMap<>();
    
    // These counters are kept for potential future enhancements
    // For example, they could be used to calculate prior probabilities in a full Bayesian model
    @SuppressWarnings("unused")
    private int totalSpam = 0;
    @SuppressWarnings("unused")
    private int totalNotSpam = 0;

    /**
     * Trains the classifier on a dataset of emails
     * 
     * @param dataset List of Email objects to learn from
     */
    @Override
    public void train(List<Email> dataset) {
        // Clear previous training data
        frequencyTable.clear();
        totalSpam = 0;
        totalNotSpam = 0;
        
        // Process each email in the dataset
        for (Email email : dataset) {
            // Create a key from the email's features
            String key = getKey(email);
            
            // Initialize counts for this feature combination if not seen before
            frequencyTable.putIfAbsent(key, new int[]{0, 0});
            
            // Update counts based on whether the email is spam or not
            if (email.isSpam()) {
                frequencyTable.get(key)[0]++;
                totalSpam++;
            } else {
                frequencyTable.get(key)[1]++;
                totalNotSpam++;
            }
        }
    }

    /**
     * Predicts whether an email is spam or not
     * 
     * @param email Email to classify
     * @return true if the email is predicted to be spam, false otherwise
     */
    @Override
    public boolean predict(Email email) {
        // An email is predicted as spam if its spam probability is greater than 50%
        return predictProbability(email) > 0.5;
    }

    /**
     * Calculates the probability that an email is spam
     * 
     * @param email Email to analyze
     * @return Probability between 0.0 (definitely not spam) and 1.0 (definitely spam)
     *         Returns -1.0 if the combination is not found in the dataset
     */
    @Override
    public double predictProbability(Email email) {
        String key = getKey(email);
        
        // If this feature combination doesn't exist in our training data, return -1
        if (!frequencyTable.containsKey(key)) {
            return -1;
        }
        
        // Get the counts for this feature combination
        int[] counts = frequencyTable.get(key);
        int spamCount = counts[0];
        int notSpamCount = counts[1];
        
        // Avoid division by zero
        if (spamCount + notSpamCount == 0) {
            return 0.5; // Neutral probability
        }
        
        // Calculate probability: P(spam | features) = spam_count / (spam_count + not_spam_count)
        return spamCount / (double)(spamCount + notSpamCount);
    }
    
    /**
     * Creates a key for the frequency table from an email's features
     * 
     * @param email Email to create key for
     * @return String key for the frequency table
     */
    private String getKey(Email email) {
        return String.join(",",
                email.getSubjectLength(),
                email.getUrgentWords(),
                email.getSenderDomain(),
                email.getAttachments(),
                email.getLinks()
        );
    }
}
