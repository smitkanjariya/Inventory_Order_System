package beans;

import ejb.ProductServiceLocal;
import entity.Products;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("inventoryAnalyticsBean")
@ViewScoped
public class InventoryAnalyticsBean implements Serializable {

    @EJB  private ProductServiceLocal productService;
    @Inject private ManagerBean       managerBean;

    private List<Products> products;

    // stock update fields
    private Integer editProductId;
    private Integer addStock;
    private Integer newReorderLevel;

    @PostConstruct
    public void init() { load(); }

    public void load() {
        products = productService.getProductsByManager(managerBean.getManager().getUserId());
    }

    // ── stock update ──────────────────────────────────────
    public void prepareEdit(Products p) {
        editProductId  = p.getProductId();
        addStock       = 0;
        newReorderLevel = p.getReorderLevel();
    }

    public void saveStock() {
        try {
            Products p = productService.getProductById(editProductId);
            if (p == null || !p.getManagerId().getUserId().equals(managerBean.getManager().getUserId())) {
                addError("Unauthorized."); return;
            }
            if (addStock != null && addStock > 0) {
                p.setStock(p.getStock() + addStock);
                p.setQuantity(p.getStock());
            }
            if (newReorderLevel != null && newReorderLevel >= 0) {
                p.setReorderLevel(newReorderLevel);
            }
            productService.updateProduct(p);
            addSuccess("Stock updated for \"" + p.getName() + "\".");
            editProductId = null; addStock = null; newReorderLevel = null;
            load();
        } catch (Exception e) {
            addError("Update failed."); e.printStackTrace();
        }
    }

    public void cancelEdit() { editProductId = null; addStock = null; newReorderLevel = null; }

    // ── chart JSON helpers ────────────────────────────────

    /** Labels JSON array — product names */
    public String getLabelsJson() {
        if (products == null || products.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            sb.append("\"").append(esc(products.get(i).getName())).append("\"");
            if (i < products.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    /** Current stock values JSON array */
    public String getStockJson() {
        return toIntArray(products, p -> p.getStock());
    }

    /** Reorder level values JSON array */
    public String getReorderJson() {
        return toIntArray(products, p -> p.getReorderLevel());
    }

    /** Total units sold per product (from order_items via quantity field) */
    public String getSoldJson() {
        // sold = original quantity - current stock  (quantity was set = stock on creation)
        return toIntArray(products, p -> Math.max(0, p.getQuantity() - p.getStock()));
    }

    /** Demand score: units sold, used for high/low demand bar chart */
    public String getDemandJson() { return getSoldJson(); }

    /** Background colors — red if stock <= reorder, orange if stock <= reorder*2, green otherwise */
    public String getStockColorsJson() {
        if (products == null || products.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            Products p = products.get(i);
            String color;
            if (p.getStock() <= p.getReorderLevel()) color = "\"rgba(239,68,68,0.8)\"";
            else if (p.getStock() <= p.getReorderLevel() * 2 + 5) color = "\"rgba(251,146,60,0.8)\"";
            else color = "\"rgba(16,185,129,0.8)\"";
            sb.append(color);
            if (i < products.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    // ── convenience counts ────────────────────────────────
    public long getLowStockCount() {
        return products == null ? 0 : products.stream().filter(p -> p.getStock() <= p.getReorderLevel()).count();
    }
    public long getHealthyStockCount() {
        return products == null ? 0 : products.stream().filter(p -> p.getStock() > p.getReorderLevel()).count();
    }
    public int getTotalStock() {
        return products == null ? 0 : products.stream().mapToInt(Products::getStock).sum();
    }

    // ── utils ─────────────────────────────────────────────
    @FunctionalInterface interface IntExtractor { int get(Products p); }

    private String toIntArray(List<Products> list, IntExtractor fn) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(fn.get(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private String esc(String s) { return s == null ? "" : s.replace("\"", "\\\""); }

    private void addSuccess(String m) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null)); }
    private void addError(String m)   { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null)); }

    public List<Products> getProducts()       { return products; }
    public Integer getEditProductId()         { return editProductId; }
    public Integer getAddStock()              { return addStock; }
    public void    setAddStock(Integer v)     { this.addStock = v; }
    public Integer getNewReorderLevel()       { return newReorderLevel; }
    public void    setNewReorderLevel(Integer v){ this.newReorderLevel = v; }
}
