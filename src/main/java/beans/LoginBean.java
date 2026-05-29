package beans;

import ejb.UserServiceLocal;
import entity.Users;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import session.UserSession;
import java.io.IOException;

@Named
@RequestScoped
public class LoginBean {

    @Inject private SecurityContext  securityContext;
    @Inject private UserSession      userSession;
    @EJB    private UserServiceLocal userService;

    private String email;
    private String password;
    private String errorMessage;

    // Quick login credentials
    private static final java.util.Map<String, String[]> QUICK_LOGIN = java.util.Map.of(
        "admin", new String[]{"admin@gmail.com", "admin123"},
        "manager", new String[]{"manager@gmail.com", "manager123"},
        "customer", new String[]{"customer@gmail.com", "customer123"},
        "staff", new String[]{"staff@gmail.com", "staff123"}
    );

    public String quickLogin(String role) {
        String[] credentials = QUICK_LOGIN.get(role.toLowerCase());
        if (credentials != null) {
            this.email = credentials[0];
            this.password = credentials[1];
            return login();
        }
        errorMessage = "Invalid role selected.";
        return null;
    }

    public String login() {
        FacesContext ctx = null;
        try {
            ctx = FacesContext.getCurrentInstance();
            HttpServletRequest  request  = (HttpServletRequest)  ctx.getExternalContext().getRequest();
            HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();

            // First, get user from database to check role
            Users user = userService.getUserByEmail(email);
            if (user == null) {
                errorMessage = "Invalid email or password!";
                return null;
            }

            if (user.getRoleId() == null) {
                errorMessage = "Account has no role assigned. Contact administrator.";
                return null;
            }

            if (!"Active".equals(user.getStatus())) {
                errorMessage = "Account is inactive. Contact administrator.";
                return null;
            }

            // Authenticate with Jakarta Security
            AuthenticationStatus status = securityContext.authenticate(
                request, response,
                AuthenticationParameters.withParams()
                    .credential(new UsernamePasswordCredential(email, new Password(password)))
            );

            if (status == AuthenticationStatus.SUCCESS) {
                // Populate session
                userSession.setUserId(user.getUserId());
                userSession.setEmail(email);
                userSession.setName(user.getName());
                userSession.setRole(user.getRoleId().getRoleName());

                String roleName = user.getRoleId().getRoleName();
                System.out.println("=== LOGIN SUCCESS ===");
                System.out.println("User: " + email);
                System.out.println("Role: " + roleName);
                System.out.println("SecurityContext roles: Admin=" + securityContext.isCallerInRole("Admin") + 
                                   ", Manager=" + securityContext.isCallerInRole("Manager") + 
                                   ", Customer=" + securityContext.isCallerInRole("Customer"));

                // Determine redirect URL based on role
                String redirectUrl = getRedirectUrlForRole(roleName, ctx);
                
                if (redirectUrl != null) {
                    System.out.println("Redirecting to: " + redirectUrl);
                    try {
                        ctx.getExternalContext().redirect(redirectUrl);
                        ctx.responseComplete();
                    } catch (IOException e) {
                        System.err.println("Redirect failed: " + e.getMessage());
                        e.printStackTrace();
                        errorMessage = "Login successful but redirect failed. Please navigate manually.";
                    }
                    return null;
                } else {
                    errorMessage = "Your role '" + roleName + "' does not have an assigned dashboard.";
                    return null;
                }
            } else if (status == AuthenticationStatus.SEND_FAILURE) {
                errorMessage = "Invalid email or password!";
                return null;
            } else if (status == AuthenticationStatus.SEND_CONTINUE) {
                ctx.responseComplete();
                return null;
            } else {
                errorMessage = "Login failed. Please try again.";
                return null;
            }

        } catch (Exception e) {
            System.err.println("Login exception: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "Login error: " + e.getMessage();
            return null;
        }
    }

    private String getRedirectUrlForRole(String roleName, FacesContext ctx) {
        String contextPath = ctx.getExternalContext().getRequestContextPath();
        
        switch (roleName) {
            case "Admin":
                return contextPath + "/admin/dashboard.xhtml";
            case "Manager":
                return contextPath + "/manager/dashboard.xhtml";
            case "Customer":
                return contextPath + "/customer/dashboard.xhtml";
            case "Staff":
                return contextPath + "/staff/dashboard.xhtml";
            default:
                return null;
        }
    }

    public String logout() {
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();
            request.logout();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            userSession.reset();
            request.getSession(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "/index.xhtml?faces-redirect=true";
    }

    public String getEmail()                          { return email; }
    public void   setEmail(String email)              { this.email = email; }

    public String getPassword()                       { return password; }
    public void   setPassword(String password)        { this.password = password; }

    public String getErrorMessage()                   { return errorMessage; }
    public void   setErrorMessage(String errorMessage){ this.errorMessage = errorMessage; }
    
    protected UserSession getUserSession()            { return userSession; }
}
