package beans;

import ejb.ManagerServiceLocal;
import ejb.UserServiceLocal;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import session.UserSession;

@Named
@SessionScoped
public class ManagerBean implements Serializable {

    @EJB private ManagerServiceLocal managerService;
    @EJB private UserServiceLocal    userService;
    @Inject private UserSession      userSession;

    private Users  manager;
    private String formOrgName;
    private long   productCount;
    private long   pendingRequestCount;

    @PostConstruct
    public void init() {
        loadManager();
    }

    private void loadManager() {
        if (userSession.getEmail() == null) return;
        manager = managerService.findByEmail(userSession.getEmail());
        if (manager != null) {
            formOrgName = manager.getOrganizationName();
            refreshStats();
        }
    }

    private void refreshStats() {
        if (manager == null) return;
        productCount        = managerService.getProductCountByManager(manager.getUserId());
        pendingRequestCount = managerService.getPendingRequestCount(manager.getUserId());
    }

    public String saveOrganization() {
        try {
            if (formOrgName == null || formOrgName.trim().isEmpty()) {
                addError("Organization name cannot be empty!");
                return null;
            }
            if (formOrgName.trim().length() > 150) {
                addError("Organization name must not exceed 150 characters!");
                return null;
            }
            managerService.updateOrganizationName(manager.getUserId(), formOrgName.trim());
            manager.setOrganizationName(formOrgName.trim());
            addSuccess("Organization name updated successfully!");
        } catch (Exception e) {
            addError("Failed to update organization name.");
        }
        return null;
    }

    public void refresh() { refreshStats(); }

    public boolean isOrgNameSet() {
        return manager != null &&
               manager.getOrganizationName() != null &&
               !manager.getOrganizationName().trim().isEmpty();
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public Users  getManager()                    { return manager; }
    public String getFormOrgName()                { return formOrgName; }
    public void   setFormOrgName(String v)        { this.formOrgName = v; }
    public long   getProductCount()               { return productCount; }
    public long   getPendingRequestCount()        { return pendingRequestCount; }
}
