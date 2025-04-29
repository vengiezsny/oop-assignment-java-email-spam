/**
 * Represents an email with features used for spam classification
 * 
 * This class encapsulates the key features of an email that are used
 * to determine if it's likely to be spam. Each feature is represented
 * as a categorical value (e.g., "Short", "Yes", "Trusted") rather than
 * raw data, as this makes classification simpler.
 */
public class Email {
    // Email features
    private final String subjectLength;   // "Short", "Medium", or "Long"
    private final String urgentWords;     // "Yes" or "No"
    private final String senderDomain;    // "Trusted", "Unknown", or "Suspicious"
    private final String attachments;     // "Yes" or "No"
    private final String links;           // "None", "Few", or "Many"
    private final boolean isSpam;         // True if this email is spam

    /**
     * Creates a new Email with the specified features
     * 
     * @param subjectLength Length category of the email subject
     * @param urgentWords Whether the email contains urgent words
     * @param senderDomain Category of the sender's domain
     * @param attachments Whether the email has attachments
     * @param links Category for the number of links in the email
     * @param isSpam Whether this email is classified as spam
     */
    public Email(String subjectLength, String urgentWords, String senderDomain, 
                String attachments, String links, boolean isSpam) {
        this.subjectLength = subjectLength;
        this.urgentWords = urgentWords;
        this.senderDomain = senderDomain;
        this.attachments = attachments;
        this.links = links;
        this.isSpam = isSpam;
    }

    /**
     * @return The subject length category ("Short", "Medium", or "Long")
     */
    public String getSubjectLength() {
        return subjectLength;
    }

    /**
     * @return Whether the email contains urgent words ("Yes" or "No")
     */
    public String getUrgentWords() {
        return urgentWords;
    }

    /**
     * @return The sender domain category ("Trusted", "Unknown", or "Suspicious")
     */
    public String getSenderDomain() {
        return senderDomain;
    }

    /**
     * @return Whether the email has attachments ("Yes" or "No")
     */
    public String getAttachments() {
        return attachments;
    }

    /**
     * @return The category for number of links ("None", "Few", or "Many")
     */
    public String getLinks() {
        return links;
    }

    /**
     * @return True if this email is classified as spam, false otherwise
     */
    public boolean isSpam() {
        return isSpam;
    }
    
    @Override
    public String toString() {
        return String.format("Email[subject=%s, urgent=%s, domain=%s, attachments=%s, links=%s, spam=%b]",
                subjectLength, urgentWords, senderDomain, attachments, links, isSpam);
    }
}
