package tn.esprit.examen.services;

import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.auth.oauth2.TokenVerifier;
import com.google.auth.oauth2.TokenVerifier.VerificationException;
import org.springframework.stereotype.Service;

@Service
public class GoogleService {

    private static final String CLIENT_ID =
            "559943847914-138movu7ml236e7d3fbtnmd4gpdpm2ag.apps.googleusercontent.com";

    private final TokenVerifier verifier;

    public GoogleService() {
        this.verifier = TokenVerifier.newBuilder()
                .setAudience(CLIENT_ID)
                .setIssuer("https://accounts.google.com")
                .build();
    }

    public JsonWebSignature verifyIdToken(String idToken) throws VerificationException {
        return verifier.verify(idToken);
    }
}
