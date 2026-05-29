package beans;

import ejb.CategoryServiceLocal;
import ejb.ProductServiceLocal;
import ejb.UserServiceLocal;
import entity.Categories;
import entity.Products;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Named("customerDashboardBean")
@SessionScoped
public class CustomerDashboardBean implements Serializable {

    @EJB private ProductServiceLocal  productService;
    @EJB private CategoryServiceLocal categoryService;
    @EJB private UserServiceLocal     userService;

    private List<Products>   allProducts  = Collections.emptyList();
    private List<Categories> categoryList = Collections.emptyList();
    private List<Users>      managerList  = Collections.emptyList();

    private String  searchQuery      = "";
    private Integer selectedCategory = 0;
    private Integer selectedManager  = 0;   // 0 = all managers
    private Products selectedProduct;

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        List<Products>   p = productService.getAllProducts();
        List<Categories> c = categoryService.getAllCategories();
        List<Users>      m = userService.getUsersByRole("Manager");
        allProducts  = p != null ? p : Collections.emptyList();
        categoryList = c != null ? c : Collections.emptyList();
        managerList  = m != null ? m : Collections.emptyList();
    }

    // ── filtered list ─────────────────────────────────────
    public List<Products> getFilteredProducts() {
        return allProducts.stream()
                .filter(p -> selectedManager == null || selectedManager == 0 ||
                        (p.getManagerId() != null &&
                         p.getManagerId().getUserId().equals(selectedManager)))
                .filter(p -> selectedCategory == null || selectedCategory == 0 ||
                        (p.getCategoryId() != null &&
                         p.getCategoryId().getCategoryId().equals(selectedCategory)))
                .filter(p -> searchQuery == null || searchQuery.isBlank() ||
                        p.getName().toLowerCase().contains(searchQuery.toLowerCase().trim()))
                .collect(Collectors.toList());
    }

    // ── manager filter ────────────────────────────────────
    public void filterByManager(Integer managerId) {
        this.selectedManager  = managerId;
        this.selectedCategory = 0;
        this.searchQuery      = "";
        reload();
    }

    public void clearManagerFilter() {
        this.selectedManager  = 0;
        this.selectedCategory = 0;
        this.searchQuery      = "";
        reload();
    }

    public String getSelectedManagerName() {
        if (selectedManager == null || selectedManager == 0) return null;
        return managerList.stream()
                .filter(m -> m.getUserId().equals(selectedManager))
                .map(m -> (m.getOrganizationName() != null && !m.getOrganizationName().isEmpty())
                        ? m.getOrganizationName()
                        : (m.getName() != null && !m.getName().isEmpty() ? m.getName() : "Unknown"))
                .findFirst().orElse(null);
    }

    // ── navigation ────────────────────────────────────────
    public String viewDetails(Products p) {
        this.selectedProduct = productService.getProductById(p.getProductId());
        return "/customer/productDetails.xhtml?faces-redirect=true";
    }

    public String backToDashboard() {
        selectedProduct = null;
        reload();
        return "/customer/dashboard.xhtml?faces-redirect=true";
    }

    // ── filter actions ────────────────────────────────────
    public void applyFilter()      { reload(); }
    public void onCategoryChange() { reload(); }
    public void clearFilter() {
        searchQuery      = "";
        selectedCategory = 0;
        selectedManager  = 0;
        reload();
    }

    // ── stat counts ───────────────────────────────────────
    public long getTotalProducts()   { return allProducts.size(); }
    public long getInStockCount()    { return allProducts.stream().filter(p -> p.getStock() > 0).count(); }
    public long getOutOfStockCount() { return allProducts.stream().filter(p -> p.getStock() == 0).count(); }

    public long getManagerProductCount(Integer managerId) {
        return allProducts.stream()
                .filter(p -> p.getManagerId() != null && p.getManagerId().getUserId().equals(managerId))
                .count();
    }

    public String getSelectedCategoryName() {
        if (selectedCategory == null || selectedCategory == 0) return "All";
        return categoryList.stream()
                .filter(c -> c.getCategoryId().equals(selectedCategory))
                .map(Categories::getCategoryName)
                .findFirst().orElse("Unknown");
    }

    // ── getters / setters ─────────────────────────────────
    public List<Categories> getCategoryList()         { return categoryList; }
    public List<Users>      getManagerList()          { return managerList; }
    public Products         getSelectedProduct()      { return selectedProduct; }

    public String  getSearchQuery()                   { return searchQuery; }
    public void    setSearchQuery(String v)           { this.searchQuery = v; }

    public Integer getSelectedCategory()              { return selectedCategory; }
    public void    setSelectedCategory(Integer v)     { this.selectedCategory = v; }

    public Integer getSelectedManager()               { return selectedManager; }
    public void    setSelectedManager(Integer v)      { this.selectedManager = v; }
}
