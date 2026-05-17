package neevhrapi.co.uk.nit.service;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CredentialStore {

    // Singleton in-memory store: refreshToken -> CredentialRecord
    private final Map<String, CredentialRecord> credentials = new ConcurrentHashMap<>();

    // Save credential context and generate a refresh token
    public String storeCredentials(String username, String applicationKey) {
        String refreshToken = UUID.randomUUID().toString();
        CredentialRecord record = new CredentialRecord(username, applicationKey, refreshToken);
        credentials.put(refreshToken, record);
        return refreshToken;
    }

    // Retrieve record by refresh token
    public CredentialRecord getCredentialsByRefreshToken(String refreshToken) {
        return credentials.get(refreshToken);
    }

    // Invalidate refresh token
    public void invalidateRefreshToken(String refreshToken) {
        credentials.remove(refreshToken);
    }

    // Internal class for storing credentials
    public static class CredentialRecord {
        private final String username;
        private final String applicationKey;
        private final String refreshToken;

        public CredentialRecord(String username, String applicationKey, String refreshToken) {
            this.username = username;
            this.applicationKey = applicationKey;
            this.refreshToken = refreshToken;
        }

        public String getUsername() {
            return username;
        }

        public String getApplicationKey() {
            return applicationKey;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}

