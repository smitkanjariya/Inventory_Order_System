package beans;

import ejb.CategoryServiceLocal;
import ejb.ManagerServiceLocal;
import entity.Categories;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import session.UserSession;
import java.io.Serializable;
import java.util.List;

@Named("categoryBean")
@ViewScoped
public class CategoryManagementBean implements Serializable {

    @EJB private CategoryServiceLocal categoryService;
    @EJB private ManagerServiceLocal managerService;
    @Inject private UserSession userSession;

    private List<Categories> categoryList;
    private Integer editCategoryId;
    private String categoryName;
    private Integer deleteCategoryId;

    @PostConstruct
    public void init() {
        loadCategories();
    }

    private void loadCategories() {
        if ("Manager".equals(userSession.getRole())) {
            Users manager = managerService.findByEmail(userSession.getEmail());
            if (manager != null) {
                categoryList = categoryService.getCategoriesByManager(manager.getUserId());
            }
        } else {
            categoryList = categoryService.getAllCategories();
        }
    }

    public String saveCategory() {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                addError("Category name is required!");
                return null;
            }

            if (categoryName.trim().length() < 2) {
                addError("Category name must be at least 2 characters!");
                return null;
            }

            if (categoryName.trim().length() > 100) {
                addError("Category name must not exceed 100 characters!");
                return null;
            }

            boolean duplicate = categoryList.stream()
                    .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(categoryName.trim())
                            && !c.getCategoryId().equals(editCategoryId));
            if (duplicate) {
                addError("Category name already exists!");
                return null;
            }

            if (editCategoryId == null) {
                Categories c = new Categories();
                c.setCategoryName(categoryName.trim());
                
                if ("Manager".equals(userSession.getRole())) {
                    Users manager = managerService.findByEmail(userSession.getEmail());
                    c.setManagerId(manager);
                }
                
                categoryService.addCategory(c);
                addSuccess("Category added successfully!");
            } else {
                Categories c = categoryService.getCategoryById(editCategoryId);
                c.setCategoryName(categoryName.trim());
                categoryService.updateCategory(c);
                addSuccess("Category updated successfully!");
            }

            resetForm();
            loadCategories();

        } catch (Exception e) {
            addError("Operation failed! Please try again.");
            e.printStackTrace();
        }
        return null;
    }

    public void editCategory(Categories c) {
        editCategoryId = c.getCategoryId();
        categoryName = c.getCategoryName();
    }

    public void deleteCategory(int id) {
        try {
            categoryService.deleteCategory(id);
            addSuccess("Category deleted successfully!");
            loadCategories();
        } catch (Exception e) {
            addError("Delete failed! Category may be linked to products.");
        }
    }

    public void prepareDelete(Integer id) {
        this.deleteCategoryId = id;
    }

    public void confirmDelete() {
        if (deleteCategoryId != null) {
            deleteCategory(deleteCategoryId);
            deleteCategoryId = null;
        }
    }

    public void resetForm() {
        editCategoryId = null;
        categoryName = null;
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public List<Categories> getCategoryList() { return categoryList; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public boolean isEditMode() { return editCategoryId != null; }
}
