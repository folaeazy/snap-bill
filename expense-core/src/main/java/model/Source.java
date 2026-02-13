package model;


/**
 * Origin of the expense data.
 * Critical for audit trail and AI confidence scoring.
 */
public enum Source {

    EMAIL_GMAIL("Gmail"),
    EMAIL_OUTLOOK("Outlook"),
    MANUAL_ENTRY("Manual"),
    RECEIPT_UPLOAD("Receipt Upload");

    private  final String displayName;

    Source(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
