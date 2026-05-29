package beans;

import ejb.ProductServiceLocal;
import ejb.CategoryServiceLocal;
import entity.Products;
import entity.Categories;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Named("productBean")
@ViewScoped
public class ProductManagementBean implements Serializable {

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private CategoryServiceLocal categoryService;

    private List<Products> productList;
    private List<Categories> categoryList;

    private Integer editProductId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer reorderLevel;
    private Integer selectedCategoryId;
    private Integer deleteProductId;

    @PostConstruct
    public void init() {
        productList = productService.getAllProducts();
        categoryList = categoryService.getAllCategories();
    }

    public String saveProduct() {
        try {
            // Name validation
            if (name == null || name.trim().isEmpty()) {
                addError("Product name is required!");
                return null;
            }
            if (name.trim().length() < 2) {
                addError("Product name must be at least 2 characters!");
                return null;
            }
            if (name.trim().length() > 100) {
                addError("Product name must not exceed 100 characters!");
                return null;
            }

            // Description validation
            if (description != null && description.trim().length() > 255) {
                addError("Description must not exceed 255 characters!");
                return null;
            }

            // Price validation
            if (price == null) {
                addError("Price is required!");
                return null;
            }
            if (price.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                addError("Price must be at least 0.01!");
                return null;
            }
            if (price.compareTo(BigDecimal.valueOf(9999999.99)) > 0) {
                addError("Price is too large!");
                return null;
            }

            // Quantity validation
            if (quantity == null) {
                addError("Quantity is required!");
                return null;
            }
            if (quantity < 0) {
                addError("Quantity cannot be negative!");
                return null;
            }

            // Reorder level validation
            if (reorderLevel == null) {
                addError("Reorder level is required!");
                return null;
            }
            if (reorderLevel < 0) {
                addError("Reorder level cannot be negative!");
                return null;
            }

            // Category validation
            if (selectedCategoryId == null || selectedCategoryId == 0) {
                addError("Please select a category!");
                return null;
            }

            Categories cat = categoryService.getCategoryById(selectedCategoryId);

            if (editProductId == null) {
                Products p = new Products();
                p.setName(name.trim());
                p.setDescription(description != null ? description.trim() : null);
                p.setPrice(price);
                p.setQuantity(quantity);
                p.setReorderLevel(reorderLevel);
                p.setCreatedAt(new Date());
                p.setCategoryId(cat);
                productService.addProduct(p);
                addSuccess("Product added successfully!");
            } else {
                Products p = productService.getProductById(editProductId);
                p.setName(name.trim());
                p.setDescription(description != null ? description.trim() : null);
                p.setPrice(price);
                p.setQuantity(quantity);
                p.setReorderLevel(reorderLevel);
                p.setCategoryId(cat);
                productService.updateProduct(p);
                addSuccess("Product updated successfully!");
            }

            resetForm();
            productList = productService.getAllProducts();

        } catch (Exception e) {
            addError("Operation failed! Please try again.");
            e.printStackTrace();
        }
        return null;
    }

    public void editProduct(Products p) {
        editProductId = p.getProductId();
        name = p.getName();
        description = p.getDescription();
        price = p.getPrice();
        quantity = p.getQuantity();
        reorderLevel = p.getReorderLevel();
        if (p.getCategoryId() != null) {
            selectedCategoryId = p.getCategoryId().getCategoryId();
        }
    }

    public void deleteProduct(int id) {
        try {
            productService.deleteProduct(id);
            addSuccess("Product deleted successfully!");
            productList = productService.getAllProducts();
        } catch (Exception e) {
            addError("Delete failed! Product may be linked to orders.");
        }
    }

    public void prepareDelete(Integer id) {
        this.deleteProductId = id;
    }

    public void confirmDelete() {
        if (deleteProductId != null) {
            deleteProduct(deleteProductId);
            deleteProductId = null;
        }
    }

    public void resetForm() {
        editProductId = null;
        name = null;
        description = null;
        price = null;
        quantity = null;
        reorderLevel = null;
        selectedCategoryId = null;
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public List<Products> getProductList() { return productList; }
    public List<Categories> getCategoryList() { return categoryList; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Integer getSelectedCategoryId() { return selectedCategoryId; }
    public void setSelectedCategoryId(Integer selectedCategoryId) { this.selectedCategoryId = selectedCategoryId; }

    public boolean isEditMode() { return editProductId != null; }
}
