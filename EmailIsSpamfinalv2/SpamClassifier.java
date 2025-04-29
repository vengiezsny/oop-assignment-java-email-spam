import java.util.List;

/**
 * Interface for spam classification algorithms
 * 
 * This interface defines the contract that any spam classifier must follow.
 * It specifies three essential methods:
 * 
 * 1. train() - Teaches the classifier using a dataset of emails
 * 2. predict() - Makes a yes/no decision about whether an email is spam
 * 3. predictProbability() - Calculates how likely an email is to be spam (0.0 to 1.0)
 * 
 * By using this interface, we can easily swap different classification algorithms
 * without changing the rest of the application. This is an example of the
 * "Strategy Pattern" in object-oriented design.
 */
public interface SpamClassifier {
    /**
     * Trains the classifier on a dataset of emails
     * @param dataset List of Email objects to learn from
     */
    void train(List<Email> dataset);
    
    /**
     * Predicts whether an email is spam or not
     * @param email Email to classify
     * @return 
     */
    boolean predict(Email email);
    
    /**
     * Calculates the probability that an email is spam
     * @param email Email to analyze
     * @return Probability between 0.0 (definitely not spam) and 1.0 (definitely spam)
     *         Returns -1.0 if the combination is not found in the dataset
     */
    double predictProbability(Email email);
}
