package beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class AdminLoginBean extends LoginBean {
    
    @Override
    public String login() {
        String result = super.login();
        
        // After successful login, verify role matches
        if (getUserSession() != null && getUserSession().getRole() != null) {
            if (!"Admin".equals(getUserSession().getRole())) {
                setErrorMessage("Access denied! This login page is for Admins only.");
                logout();
                return null;
            }
        }
        
        return result;
    }
}
