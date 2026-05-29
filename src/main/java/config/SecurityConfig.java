package config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage          = "/login.xhtml",
        errorPage          = "/login.xhtml",
        useForwardToLogin  = false          // redirect (302) not forward — required for JSF navigation
    )
)
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "jdbc/inventoryDS",
    callerQuery      = "SELECT password FROM users WHERE email = ?",
    groupsQuery      = "SELECT r.role_name FROM roles r JOIN users u ON u.role_id = r.role_id WHERE u.email = ?",
    hashAlgorithm    = Pbkdf2PasswordHash.class,
    hashAlgorithmParameters = {
        "Pbkdf2PasswordHash.Iterations=2048",
        "Pbkdf2PasswordHash.Algorithm=PBKDF2WithHmacSHA256",
        "Pbkdf2PasswordHash.SaltSizeBytes=32"
    },
    priority = 30
)
@ApplicationScoped
public class SecurityConfig {
}
