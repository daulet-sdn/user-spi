package kz.eub;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static kz.eub.LegacyUserModel.*;

public class LegacyUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<UserModelKey, UserModel> loadedUsers = new ConcurrentHashMap<>();

    public LegacyUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        this.userRepository = new UserRepository(session, model);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.endsWith(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        final var challengeResponse = credentialInput.getChallengeResponse();
        final var savedUser = getUserByUsername(user.getUsername(), realm);
        final var hash = savedUser.getFirstAttribute(PASSWORD);
        final var sha = sha64(challengeResponse);
        final var result = Objects.equals(hash, sha);

        if (result) {
            session.userCredentialManager().updateCredential(
                    realm,
                    user,
                    UserCredentialModel.password(challengeResponse)
            );
        }

        return result;
    }

    private String sha64(String str) {
        if (Objects.isNull(str)) {
            return "";
        }
        final var sha = DigestUtils.sha1(str);
        return Base64.encodeBase64String(sha);
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        return null;
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        final var userModelKey  = new UserModelKey(username, realm.getId());
        final var adapter = loadedUsers
                .computeIfAbsent(userModelKey,
                        val -> userRepository.findUserByUsername(realm, username)
                                .orElseThrow(() -> new RuntimeException("User not found!")));
        return createAdapter(realm, adapter);
    }

    private UserModel createAdapter(RealmModel realm, UserModel userModel) {
        var local = session.userLocalStorage().getUserByUsername(realm, userModel.getUsername());
        if (Objects.isNull(local)) {
            local = session.userLocalStorage().addUser(realm, userModel.getUsername());
            local.setAttribute(FIRST_NAME, Collections.singletonList(userModel.getFirstAttribute(FIRST_NAME)));
            local.setAttribute(LAST_NAME, Collections.singletonList(userModel.getFirstAttribute(LAST_NAME)));
            local.setEnabled(true);
            local.setFederationLink(model.getId());
        }
        return local;
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        return null;
    }

    private static class UserModelKey {
        public final String username;
        public final String realmId;

        public UserModelKey(String username, String realmId) {
            this.username = username;
            this.realmId = realmId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserModelKey)) return false;
            UserModelKey that = (UserModelKey) o;
            return Objects.equals(username, that.username) && Objects.equals(realmId, that.realmId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, realmId);
        }
    }
}
