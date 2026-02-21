package expense.email.service;



import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import expense.email.model.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailService {

    private static final String APPLICATION_NAME = "SnapBill Gmail Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER = "me"; // "me" = authenticated user

    private final EmailTokenService tokenService;

    /**
     * Fetch Recent Email method
     * @param emailAccount object
     * @param maxResult ...
     * @return type of list of recent emails
     */

    public List<EmailMessage> fetchRecentEmails(EmailAccount emailAccount, int maxResult) throws IOException, GeneralSecurityException {


    }
}
