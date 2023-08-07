package kz.eub;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.sql.SQLException;
import java.util.List;

import static kz.eub.DBConstants.*;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class LegacyUserStorageProviderFactory implements UserStorageProviderFactory<LegacyUserStorageProvider> {

    private static final String PROVIDER_ID = "legacy-provider";
    protected List<ProviderConfigProperty> configProperties;

    @Override
    public void init(Config.Scope config) {

        final var builder = ProviderConfigurationBuilder.create();

        builder.property(DATASOURCE_DRIVER_CLASS.name(), "JDBC Driver Class", "JDBC Driver Class", STRING_TYPE, null, null);
        builder.property(DATASOURCE_URL.name(), "JDBC Url", "JDBC Url", STRING_TYPE, null, null);
        builder.property(DATASOURCE_USERNAME.name(), "DB Username", "DB Username", STRING_TYPE, null, null);
        builder.property(DATASOURCE_PASSWORD.name(), "DB Password", "DB Password", STRING_TYPE, null, null);

        configProperties = builder.build();
    }

    @Override
    public LegacyUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new LegacyUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        try (final var connection = DataStorage.getConnection(config)) {
            connection.createStatement().execute("SELECT 1;");
        } catch (SQLException e) {
            throw new ComponentValidationException(String.format("Database connection refused: %s", e.getMessage()));
        }
    }
}
