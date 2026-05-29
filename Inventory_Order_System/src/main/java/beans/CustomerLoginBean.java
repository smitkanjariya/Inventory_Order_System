package beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class CustomerLoginBean extends LoginBean {
    
    @Override
    public String login() {
        String result = super.login();
        
        // After successful login, verify role matches
        if (getUserSession() != null && getUserSession().getRole() != null) {
            if (!"Customer".equals(getUserSession().getRole())) {
                setErrorMessage("Access denied! This login page is for Customers only.");
                logout();
                return null;
            }
        }
        
        return result;
    }
}
