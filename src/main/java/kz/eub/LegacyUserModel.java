package kz.eub;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.HashMap;
import java.util.Map;

public class LegacyUserModel extends AbstractUserAdapter {

    public static final String PASSWORD = "password";

    private final Map<String, String> attributes = new HashMap<>();

    public LegacyUserModel(KeycloakSession session,
                           RealmModel realm,
                           ComponentModel storageProviderModel,
                           String username,
                           String firstName,
                           String lastName,
                           String password) {
        super(session, realm, storageProviderModel);
        this.attributes.put(USERNAME, username);
        this.attributes.put(FIRST_NAME, firstName);
        this.attributes.put(LAST_NAME, lastName);
        this.attributes.put(PASSWORD, password);
    }

    @Override
    public String getUsername() {
        return getFirstAttribute(USERNAME);
    }

    @Override
    public String getFirstAttribute(String name) {
        return attributes.get(name);
    }

    public static class Builder {
        private KeycloakSession session;
        private RealmModel realm;
        private ComponentModel model;
        private String username;
        private String firstName;
        private String lastName;
        private String password;

        public Builder session(KeycloakSession session) {
            this.session = session;
            return this;
        }

        public Builder realm(RealmModel realm) {
            this.realm = realm;
            return this;
        }

        public Builder model(ComponentModel model) {
            this.model = model;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public LegacyUserModel build() {
            return new LegacyUserModel(
                    session,
                    realm,
                    model,
                    username,
                    firstName,
                    lastName,
                    password
            );
        }
    }
}
