package kz.eub;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {

    private final KeycloakSession session;
    private final ComponentModel model;

    public UserRepository(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    public Optional<LegacyUserModel> findUserByUsername(RealmModel realm, String username) {
        final var SQL_REQUEST = "SELECT * " +
                "FROM User u " +
                "WHERE u.username = ?";

        try (final var connection = DataStorage.getConnection(this.model)) {
            final var prepareStatement = connection.prepareStatement(SQL_REQUEST);
            prepareStatement.setString(1, username);
            prepareStatement.execute();
            final var result = prepareStatement.getResultSet();

            if (!result.next()) {
                return Optional.empty();
            }

            final var userModel = new LegacyUserModel.Builder()
                    .session(session)
                    .realm(realm)
                    .model(model)
                    .username(result.getString(1))
                    .firstName(result.getString(2))
                    .lastName(result.getString(3))
                    .password(result.getString(4))
                    .build();

            return Optional.of(userModel);

        } catch (SQLException e) {
            throw new RuntimeException(String.format("SQL error: %s", e.getMessage()));
        }
    }
}
