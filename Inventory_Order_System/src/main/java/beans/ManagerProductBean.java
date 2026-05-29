package beans;

import ejb.CategoryServiceLocal;
import ejb.ManagerServiceLocal;
import ejb.ProductServiceLocal;
import entity.Categories;
import entity.Products;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Named("managerProductBean")
@ViewScoped
public class ManagerProductBean implements Serializable {

    @EJB private ProductServiceLocal  productService;
    @EJB private CategoryServiceLocal categoryService;
    @EJB private ManagerServiceLocal  managerService;

    @Inject private ManagerBean managerBean;

    // ── lists ──────────────────────────────────────────────
    private List<Products>   productList;
    private List<Categories> categoryList;

    // ── category form ──────────────────────────────────────
    private Integer editCategoryId;
    private String  categoryName;
    private Integer deleteCategoryId;

    // ── product form ───────────────────────────────────────
    private Integer    editProductId;
    private String     productName;
    private String     description;
    private BigDecimal price;
    private Integer    stock;
    private Integer    minOrderQuantity;
    private Part       uploadedFile;
    private String     productImage;
    private Integer    selectedCategoryId;
    private Integer    deleteProductId;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    // Payara docroot — permanent storage, never wiped on redeploy
    private static final String UPLOAD_DIR = filter.ProductImageServlet.UPLOAD_DIR;

    // ──────────────────────────────────────────────────────
    @PostConstruct
    public void init() {
        loadCategories();
        refreshProductList();
    }

    private void loadCategories() {
        Users m = managerBean.getManager();
        if (m != null && m.getUserId() != null) {
            categoryList = categoryService.getCategoriesByManagerIncludingShared(m.getUserId());
        }
    }

    private void refreshProductList() {
        Users m = managerBean.getManager();
        if (m != null && m.getUserId() != null) {
            productList = productService.getProductsByManager(m.getUserId());
        }
    }

    // ── CATEGORY CRUD ──────────────────────────────────────

    public void saveCategory() {
        try {
            if (categoryName == null || categoryName.trim().length() < 2) {
                addError("Category name must be at least 2 characters!");
                return;
            }
            if (categoryName.trim().length() > 100) {
                addError("Category name must not exceed 100 characters!");
                return;
            }
            boolean duplicate = categoryList.stream()
                    .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(categoryName.trim())
                            && !c.getCategoryId().equals(editCategoryId));
            if (duplicate) {
                addError("Category name already exists!");
                return;
            }

            if (editCategoryId == null) {
                Categories c = new Categories();
                c.setCategoryName(categoryName.trim());
                Users m = managerBean.getManager();
                c.setManagerId(m);
                categoryService.addCategory(c);
                addSuccess("Category added successfully!");
            } else {
                Categories c = categoryService.getCategoryById(editCategoryId);
                c.setCategoryName(categoryName.trim());
                categoryService.updateCategory(c);
                addSuccess("Category updated successfully!");
            }
            resetCategoryForm();
            loadCategories();
        } catch (Exception e) {
            addError("Operation failed! Please try again.");
            e.printStackTrace();
        }
    }

    public void editCategory(Categories c) {
        editCategoryId = c.getCategoryId();
        categoryName   = c.getCategoryName();
    }

    public void prepareDeleteCategory(Integer id) { this.deleteCategoryId = id; }

    public void confirmDeleteCategory() {
        if (deleteCategoryId == null) return;
        try {
            categoryService.deleteCategory(deleteCategoryId);
            addSuccess("Category deleted successfully!");
            loadCategories();
        } catch (Exception e) {
            addError("Delete failed! Category may be linked to products.");
        }
        deleteCategoryId = null;
    }

    public void resetCategoryForm() {
        editCategoryId = null;
        categoryName   = null;
    }

    // ── PRODUCT CRUD ───────────────────────────────────────

    public void saveProduct() {
        try {
            Users m = managerBean.getManager();
            if (m == null) { addError("Manager session not found!"); return; }

            if (productName == null || productName.trim().length() < 2) {
                addError("Product name must be at least 2 characters!");
                return;
            }
            if (productName.trim().length() > 100) {
                addError("Product name must not exceed 100 characters!");
                return;
            }
            if (description != null && description.trim().length() > 255) {
                addError("Description must not exceed 255 characters!");
                return;
            }
            if (price == null || price.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                addError("Price must be at least 0.01!");
                return;
            }
            if (stock == null || stock < 0) {
                addError("Stock cannot be negative!");
                return;
            }
            if (minOrderQuantity == null || minOrderQuantity < 1) {
                addError("Minimum order quantity must be at least 1!");
                return;
            }
            if (selectedCategoryId == null || selectedCategoryId == 0) {
                addError("Please select a category!");
                return;
            }

            // Handle file upload
            String imagePath = productImage; // Keep existing image for edit
            if (uploadedFile != null && uploadedFile.getSize() > 0) {
                imagePath = handleFileUpload();
                if (imagePath == null) return; // Error already shown
            }

            Categories cat = categoryService.getCategoryById(selectedCategoryId);

            if (editProductId == null) {
                Products p = new Products();
                p.setName(productName.trim());
                p.setDescription(description != null ? description.trim() : null);
                p.setPrice(price);
                p.setStock(stock);
                p.setQuantity(stock);
                p.setMinOrderQuantity(minOrderQuantity);
                p.setProductImage(imagePath);
                p.setReorderLevel(0);
                p.setCreatedAt(new Date());
                p.setCategoryId(cat);
                p.setManagerId(m);
                productService.addProduct(p);
                addSuccess("Product added successfully!");
            } else {
                Products p = productService.getProductById(editProductId);
                if (p.getManagerId() == null || !p.getManagerId().getUserId().equals(m.getUserId())) {
                    addError("You can only edit your own products!");
                    return;
                }
                p.setName(productName.trim());
                p.setDescription(description != null ? description.trim() : null);
                p.setPrice(price);
                p.setStock(stock);
                p.setQuantity(stock);
                p.setMinOrderQuantity(minOrderQuantity);
                if (imagePath != null) {
                    p.setProductImage(imagePath);
                }
                p.setCategoryId(cat);
                productService.updateProduct(p);
                addSuccess("Product updated successfully!");
            }

            resetProductForm();
            refreshProductList();
            managerBean.refresh();
        } catch (Exception e) {
            addError("Operation failed! Please try again.");
            e.printStackTrace();
        }
    }

    private String handleFileUpload() {
        try {
            if (uploadedFile.getSize() > MAX_FILE_SIZE) {
                addError("File size must not exceed 5MB!");
                return null;
            }

            String fileName = uploadedFile.getSubmittedFileName();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                addError("Only image files (jpg, png, gif, webp) are allowed!");
                return null;
            }

            String uniqueFileName = UUID.randomUUID().toString() + extension;
            
            File uploadDir = new File(UPLOAD_DIR);
            
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File file = new File(uploadDir, uniqueFileName);
            try (InputStream input = uploadedFile.getInputStream()) {
                Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Store only the filename — served via /product-images/filename
            return uniqueFileName;

        } catch (Exception e) {
            addError("Failed to upload image!");
            e.printStackTrace();
            return null;
        }
    }

    public void editProduct(Products p) {
        editProductId      = p.getProductId();
        productName        = p.getName();
        description        = p.getDescription();
        price              = p.getPrice();
        stock              = p.getStock();
        minOrderQuantity   = p.getMinOrderQuantity();
        productImage       = p.getProductImage();
        selectedCategoryId = p.getCategoryId() != null ? p.getCategoryId().getCategoryId() : null;
    }

    public void prepareDeleteProduct(Integer id) { this.deleteProductId = id; }

    public void confirmDeleteProduct() {
        if (deleteProductId == null) return;
        try {
            Users m = managerBean.getManager();
            Products p = productService.getProductById(deleteProductId);
            if (p.getManagerId() == null || !p.getManagerId().getUserId().equals(m.getUserId())) {
                addError("You can only delete your own products!");
                deleteProductId = null;
                return;
            }
            productService.deleteProduct(deleteProductId);
            addSuccess("Product deleted successfully!");
            refreshProductList();
            managerBean.refresh();
        } catch (Exception e) {
            addError("Delete failed! Product may be linked to orders.");
        }
        deleteProductId = null;
    }

    public void resetProductForm() {
        editProductId      = null;
        productName        = null;
        description        = null;
        price              = null;
        stock              = null;
        minOrderQuantity   = null;
        uploadedFile       = null;
        productImage       = null;
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

    // ── getters / setters ──────────────────────────────────
    public List<Products>   getProductList()   { return productList; }
    public List<Categories> getCategoryList()  { return categoryList; }

    public boolean isCategoryEditMode() { return editCategoryId != null; }
    public boolean isProductEditMode()  { return editProductId  != null; }

    public String  getCategoryName()                          { return categoryName; }
    public void    setCategoryName(String v)                  { this.categoryName = v; }

    public String  getProductName()                           { return productName; }
    public void    setProductName(String v)                   { this.productName = v; }

    public String  getDescription()                           { return description; }
    public void    setDescription(String v)                   { this.description = v; }

    public BigDecimal getPrice()                              { return price; }
    public void       setPrice(BigDecimal v)                  { this.price = v; }

    public Integer getStock()                                 { return stock; }
    public void    setStock(Integer v)                        { this.stock = v; }

    public Integer getMinOrderQuantity()                      { return minOrderQuantity; }
    public void    setMinOrderQuantity(Integer v)             { this.minOrderQuantity = v; }

    public Part    getUploadedFile()                          { return uploadedFile; }
    public void    setUploadedFile(Part v)                    { this.uploadedFile = v; }

    public String  getProductImage()                          { return productImage; }
    public void    setProductImage(String v)                  { this.productImage = v; }

    public Integer getSelectedCategoryId()                    { return selectedCategoryId; }
    public void    setSelectedCategoryId(Integer v)           { this.selectedCategoryId = v; }
}
