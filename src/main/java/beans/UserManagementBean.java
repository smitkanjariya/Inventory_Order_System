package beans;

import ejb.UserServiceLocal;
import entity.Roles;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class UserManagementBean implements Serializable {

    @EJB
    private UserServiceLocal userService;

    private List<Users> userList;
    private List<Roles> roleList;

    private Integer editUserId;
    private String formName;
    private String formEmail;
    private String formPassword;
    private String formPhone;
    private String formOrganizationName;
    private String formAddress;
    private int formRoleId;
    private Integer deleteUserId;

    @PostConstruct
    public void init() {
        userList = userService.getAllUsers();
        roleList = userService.getAllRoles();
    }

    public void loadUserForEdit(Users user) {
        if (user == null) { resetForm(); return; }
        editUserId = user.getUserId();
        formName = user.getName();
        formEmail = user.getEmail();
        formPassword = "";
        formPhone = user.getPhone();
        formOrganizationName = user.getOrganizationName();
        formAddress = user.getAddress();
        formRoleId = user.getRoleId().getRoleId();
    }

    public void resetForm() {
        editUserId = null;
        formName = null;
        formEmail = null;
        formPassword = null;
        formPhone = null;
        formOrganizationName = null;
        formAddress = null;
        formRoleId = 0;
    }

    public String saveUser() {
        try {
            // Name validation
            if (formName == null || formName.trim().isEmpty()) {
                addError("Full name is required!");
                return null;
            }
            if (formName.trim().length() < 2) {
                addError("Name must be at least 2 characters!");
                return null;
            }
            if (formName.trim().length() > 100) {
                addError("Name must not exceed 100 characters!");
                return null;
            }

            // Role validation
            if (formRoleId == 0) {
                addError("Please select a role!");
                return null;
            }

            Roles role = userService.getAllRoles().stream()
                    .filter(r -> r.getRoleId() == formRoleId)
                    .findFirst().orElse(null);
            if (role == null) {
                addError("Selected role not found!");
                return null;
            }

            if (editUserId == null) {
                // CREATE MODE validations
                if (formEmail == null || formEmail.trim().isEmpty()) {
                    addError("Email is required!");
                    return null;
                }
                // Email format validation
                if (!formEmail.trim().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                    addError("Please enter a valid email address!");
                    return null;
                }
                if (formEmail.trim().length() > 100) {
                    addError("Email must not exceed 100 characters!");
                    return null;
                }

                // Password validation
                if (formPassword == null || formPassword.trim().isEmpty()) {
                    addError("Password is required!");
                    return null;
                }
                if (formPassword.length() < 6) {
                    addError("Password must be at least 6 characters!");
                    return null;
                }
                if (formPassword.length() > 50) {
                    addError("Password must not exceed 50 characters!");
                    return null;
                }

                // Phone validation (optional but if provided must be valid)
                if (formPhone != null && !formPhone.trim().isEmpty()) {
                    if (!formPhone.trim().matches("^[0-9+\\-\\s]{7,15}$")) {
                        addError("Phone number must be 7-15 digits!");
                        return null;
                    }
                }

                // Organization name length
                if (formOrganizationName != null && formOrganizationName.trim().length() > 100) {
                    addError("Organization name must not exceed 100 characters!");
                    return null;
                }

                // Address length
                if (formAddress != null && formAddress.trim().length() > 255) {
                    addError("Address must not exceed 255 characters!");
                    return null;
                }

                // Duplicate email check
                if (userService.getUserByEmail(formEmail.trim()) != null) {
                    addError("Email already exists!");
                    return null;
                }

                String hashedPassword = hashPassword(formPassword);

                Users user = new Users();
                user.setName(formName.trim());
                user.setEmail(formEmail.trim());
                user.setPassword(hashedPassword);
                user.setPhone(formPhone != null ? formPhone.trim() : null);
                user.setOrganizationName(formOrganizationName != null ? formOrganizationName.trim() : null);
                user.setAddress(formAddress != null ? formAddress.trim() : null);
                user.setStatus("Active");
                user.setRoleId(role);
                user.setCreatedAt(new Date());
                userService.registerUser(user);
                addSuccess("User created successfully!");

            } else {
                // EDIT MODE validations
                if (formName.trim().length() < 2) {
                    addError("Name must be at least 2 characters!");
                    return null;
                }

                // Phone validation (optional)
                if (formPhone != null && !formPhone.trim().isEmpty()) {
                    if (!formPhone.trim().matches("^[0-9+\\-\\s]{7,15}$")) {
                        addError("Phone number must be 7-15 digits!");
                        return null;
                    }
                }

                // Organization name length
                if (formOrganizationName != null && formOrganizationName.trim().length() > 100) {
                    addError("Organization name must not exceed 100 characters!");
                    return null;
                }

                // Address length
                if (formAddress != null && formAddress.trim().length() > 255) {
                    addError("Address must not exceed 255 characters!");
                    return null;
                }

                // New password validation (only if provided)
                if (formPassword != null && !formPassword.trim().isEmpty()) {
                    if (formPassword.length() < 6) {
                        addError("New password must be at least 6 characters!");
                        return null;
                    }
                    if (formPassword.length() > 50) {
                        addError("Password must not exceed 50 characters!");
                        return null;
                    }
                }

                Users user = userService.getUserById(editUserId);
                user.setName(formName.trim());
                user.setPhone(formPhone != null ? formPhone.trim() : null);
                user.setOrganizationName(formOrganizationName != null ? formOrganizationName.trim() : null);
                user.setAddress(formAddress != null ? formAddress.trim() : null);
                user.setRoleId(role);
                if (formPassword != null && !formPassword.trim().isEmpty()) {
                    user.setPassword(hashPassword(formPassword));
                }
                userService.updateUser(user);
                addSuccess("User updated successfully!");
            }

            resetForm();
            userList = userService.getAllUsers();
            return null;

        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            addError("Operation failed: " + cause.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String deleteUser(int userId) {
        try {
            userService.deleteUser(userId);
            addSuccess("User deleted successfully!");
            userList = userService.getAllUsers();
        } catch (Exception e) {
            addError("Failed to delete user.");
        }
        return null;
    }

    public String toggleStatus(Users user) {
        try {
            user.setStatus(user.getStatus().equals("Active") ? "Inactive" : "Active");
            userService.updateUser(user);
            userList = userService.getAllUsers();
        } catch (Exception e) {
            addError("Failed to update status.");
        }
        return null;
    }

    public void prepareDelete(Integer userId) {
        this.deleteUserId = userId;
    }

    public String confirmDelete() {
        if (deleteUserId != null) {
            return deleteUser(deleteUserId);
        }
        return null;
    }

    private String hashPassword(String password) {
        Pbkdf2PasswordHash passwordHash = CDI.current().select(Pbkdf2PasswordHash.class).get();
        Map<String, String> params = new HashMap<>();
        params.put("Pbkdf2PasswordHash.Iterations", "2048");
        params.put("Pbkdf2PasswordHash.Algorithm", "PBKDF2WithHmacSHA256");
        params.put("Pbkdf2PasswordHash.SaltSizeBytes", "32");
        passwordHash.initialize(params);
        return passwordHash.generate(password.toCharArray());
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public boolean isEditMode() { return editUserId != null; }

    public List<Users> getUserList() { return userList; }
    public List<Roles> getRoleList() { return roleList; }
    public Integer getEditUserId() { return editUserId; }
    public Integer getDeleteUserId() { return deleteUserId; }
    public void setDeleteUserId(Integer deleteUserId) { this.deleteUserId = deleteUserId; }
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public String getFormEmail() { return formEmail; }
    public void setFormEmail(String formEmail) { this.formEmail = formEmail; }
    public String getFormPassword() { return formPassword; }
    public void setFormPassword(String formPassword) { this.formPassword = formPassword; }
    public String getFormPhone() { return formPhone; }
    public void setFormPhone(String formPhone) { this.formPhone = formPhone; }
    public String getFormOrganizationName() { return formOrganizationName; }
    public void setFormOrganizationName(String formOrganizationName) { this.formOrganizationName = formOrganizationName; }
    public String getFormAddress() { return formAddress; }
    public void setFormAddress(String formAddress) { this.formAddress = formAddress; }
    public int getFormRoleId() { return formRoleId; }
    public void setFormRoleId(int formRoleId) { this.formRoleId = formRoleId; }
}
