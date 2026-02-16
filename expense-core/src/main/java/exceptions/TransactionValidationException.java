package exceptions;


import java.util.ArrayList;
import java.util.List;

/**
 * Collects multiple validation failures for a Transaction.

 * Useful when validating a partially constructed transaction (e.g. from AI parsing)
 * and you want to return all problems at once instead of failing on the first one.
 */
public class TransactionValidationException extends DomainValidationException{

    private final List<String> errors = new ArrayList<>();

    public TransactionValidationException() {
        super("Transaction validation failed");
    }

    public TransactionValidationException(String message) {
        super(message);
    }

    public void addError(String errorMessage) {
        errors.add(errorMessage);
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    @Override
    public String getMessage() {
        if (errors.isEmpty()) {
            return super.getMessage();
        }

        StringBuilder sb = new StringBuilder("Transaction validation failed (");
        sb.append(errors.size()).append(" errors):\n");
        for (int i = 0; i < errors.size(); i++) {
            sb.append("  ").append(i + 1).append(") ").append(errors.get(i)).append("\n");
        }
        return sb.toString();
    }
}
