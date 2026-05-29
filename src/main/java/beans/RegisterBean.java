package beans;

import ejb.UserServiceLocal;
import entity.Roles;
import entity.Users;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named
@RequestScoped
public class RegisterBean {

    @EJB
    private UserServiceLocal userService;

    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String organizationName;
    private String address;
    private String errorMessage;
    private String successMessage;

    public String register() {
        try {
            // Check passwords match
            if (!password.equals(confirmPassword)) {
                errorMessage = "Passwords do not match!";
                return null;
            }

            // Check email already exists
            if (userService.getUserByEmail(email) != null) {
                errorMessage = "Email already registered!";
                return null;
            }

            // Hash password using Pbkdf2
            Pbkdf2PasswordHash passwordHash = CDI.current().select(Pbkdf2PasswordHash.class).get();
            Map<String, String> params = new HashMap<>();
            params.put("Pbkdf2PasswordHash.Iterations", "2048");
            params.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA256");
            params.put("Pbkdf2PasswordHash.SaltSizeBytes", "32");
            passwordHash.initialize(params);
            String hashedPassword = passwordHash.generate(password.toCharArray());

            // Get Customer role from EJB
            Roles customerRole = userService.getCustomerRole();

            // Create user
            Users user = new Users();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(hashedPassword);
            user.setPhone(phone);
            user.setOrganizationName(organizationName);
            user.setAddress(address);
            user.setStatus("Active");
            user.setRoleId(customerRole);
            user.setCreatedAt(new Date());

            userService.registerUser(user);

            return "/login?faces-redirect=true";

        } catch (Exception e) {
            errorMessage = "Registration failed. Please try again.";
            e.printStackTrace();
            return null;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSuccessMessage() { return successMessage; }
    public void setSuccessMessage(String successMessage) { this.successMessage = successMessage; }
}
