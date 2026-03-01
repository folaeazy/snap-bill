package com.infrastructure.email.outlook;

import com.domain.entities.EmailAccount;
import com.domain.gateways.EmailGateway;
import com.domain.model.EmailMessage;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component("outlookEmailGateway")
@RequiredArgsConstructor
@Slf4j
public class OutlookEmailGatewayImpl implements EmailGateway {
    /**
     * @param account The connected email account with provider details and tokens
     * @param since   The timestamp of the last successful sync
     * @return list of email message object
     */
    @Override
    public List<EmailMessage> fetchNewMessages(EmailAccount account, Instant since) {
        return List.of();
    }

    /**
     * @param account
     * @return boolean
     */
    @Override
    public boolean isConnectionValid(EmailAccount account) {
        return false;
    }


    //  Helper methods

    private GraphServiceClient createGraphClient(EmailAccount account) {
        String accessToken = refreshAccessTokenIfNeeded(account);

        return null;
    }

    private String refreshAccessTokenIfNeeded(EmailAccount account) {
        return "";
    }
}
