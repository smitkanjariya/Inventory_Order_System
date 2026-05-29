package beans;

import ejb.CartServiceLocal;
import ejb.ManagerServiceLocal;
import ejb.ProductServiceLocal;
import ejb.UserServiceLocal;
import entity.Cart;
import entity.Products;
import entity.Request;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import session.UserSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Named("cartBean")
@SessionScoped
public class CartBean implements Serializable {

    @EJB private CartServiceLocal    cartService;
    @EJB private ProductServiceLocal productService;
    @EJB private UserServiceLocal    userService;
    @EJB private ManagerServiceLocal managerService;

    @Inject private UserSession userSession;
    @Inject private CustomerDashboardBean customerDashboardBean;

    private List<Cart> cartItems;
    private int        addQuantity = 1;   // bound to quantity input on details page

    @PostConstruct
    public void init() {
        loadCart();
    }

    private void loadCart() {
        try {
            if (userSession != null && userSession.getUserId() != null) {
                cartItems = cartService.getCartByCustomer(userSession.getUserId());
            }
        } catch (Exception e) {
            // UserSession not ready yet, will load on first access
            cartItems = null;
        }
    }

    public void initQuantity(jakarta.faces.event.ComponentSystemEvent event) {
        Products product = customerDashboardBean != null ? customerDashboardBean.getSelectedProduct() : null;
        if (product != null && product.getMinOrderQuantity() != null && product.getMinOrderQuantity() > 1) {
            addQuantity = product.getMinOrderQuantity();
        } else {
            addQuantity = 1;
        }
    }

    // ── ADD TO CART ───────────────────────────────────────

    public String addToCart(Products product) {
        try {
            if (addQuantity <= 0) {
                addError("Quantity must be at least 1.");
                return null;
            }

            Products fresh = productService.getProductById(product.getProductId());

            if (fresh.getStock() == 0) {
                addError("\"" + fresh.getName() + "\" is out of stock.");
                return null;
            }
            
            // Check minimum order quantity
            if (fresh.getMinOrderQuantity() != null && addQuantity < fresh.getMinOrderQuantity()) {
                addError("Minimum order quantity for \"" + fresh.getName() + "\" is " + fresh.getMinOrderQuantity() + " units.");
                return null;
            }
            
            if (addQuantity > fresh.getStock()) {
                addError("Only " + fresh.getStock() + " units available.");
                return null;
            }

            int customerId = userSession.getUserId();

            // if already in cart → increment quantity
            Cart existing = cartService.getCartItem(customerId, fresh.getProductId());
            if (existing != null) {
                int newQty = existing.getQuantity() + addQuantity;
                if (newQty > fresh.getStock()) {
                    addError("Cannot add more. Cart already has " + existing.getQuantity()
                            + " unit(s). Only " + fresh.getStock() + " available.");
                    return null;
                }
                existing.setQuantity(newQty);
                cartService.updateCart(existing);
                addSuccess("\"" + fresh.getName() + "\" quantity updated in cart.");
            } else {
                Users customer = userService.getUserById(customerId);
                Cart cart = new Cart();
                cart.setCustomerId(customer);
                cart.setProductId(fresh);
                cart.setQuantity(addQuantity);
                cartService.addToCart(cart);
                addSuccess("\"" + fresh.getName() + "\" added to cart.");
            }

            addQuantity = 1;
            loadCart();
            return null;

        } catch (Exception e) {
            addError("Failed to add to cart.");
            e.printStackTrace();
            return null;
        }
    }

    // ── UPDATE QUANTITY ───────────────────────────────────

    public void updateQuantity(Cart item, int delta) {
        try {
            int newQty = item.getQuantity() + delta;
            Products fresh = productService.getProductById(item.getProductId().getProductId());
            int minQty = fresh.getMinOrderQuantity() != null ? fresh.getMinOrderQuantity() : 1;
            
            if (newQty < minQty) {
                addError("Minimum order quantity is " + minQty + " units.");
                return;
            }
            if (newQty > fresh.getStock()) {
                addError("Only " + fresh.getStock() + " units available.");
                return;
            }
            item.setQuantity(newQty);
            cartService.updateCart(item);
            loadCart();
        } catch (Exception e) {
            addError("Failed to update quantity.");
        }
    }

    // ── REMOVE ITEM ───────────────────────────────────────

    public void removeItem(Cart item) {
        try {
            cartService.removeFromCart(item.getCartId());
            addSuccess("Item removed from cart.");
            loadCart();
        } catch (Exception e) {
            addError("Failed to remove item.");
        }
    }

    // ── SEND REQUESTS ─────────────────────────────────────

    public void sendRequests() {
        try {
            if (cartItems == null || cartItems.isEmpty()) {
                addError("Your cart is empty.");
                return;
            }

            Users customer = userService.getUserById(userSession.getUserId());
            
            // Check for existing pending requests
            List<Request> existingRequests = managerService.getRequestsByCustomer(userSession.getUserId());
            
            int sent = 0;

            for (Cart item : cartItems) {
                Products product = productService.getProductById(
                        item.getProductId().getProductId());

                if (product.getManagerId() == null) {
                    addError("\"" + product.getName() + "\" has no manager assigned — skipped.");
                    continue;
                }

                // Check if request already exists for this product
                boolean alreadyRequested = existingRequests.stream()
                    .anyMatch(r -> r.getProductId().getProductId().equals(product.getProductId()) 
                                && r.getStatus() == Request.Status.PENDING);
                
                if (alreadyRequested) {
                    addError("\"" + product.getName() + "\" already has a pending request — skipped.");
                    continue;
                }

                if (product.getStock() < item.getQuantity()) {
                    addError("\"" + product.getName() + "\": only " + product.getStock()
                            + " units available, requested " + item.getQuantity() + " — skipped.");
                    continue;
                }

                Request req = new Request();
                req.setCustomerId(customer);
                req.setProductId(product);
                req.setManagerId(product.getManagerId());
                req.setQuantity(item.getQuantity());
                req.setStatus(Request.Status.PENDING);
                req.setCreatedAt(new Date());

                managerService.saveRequest(req);
                sent++;
            }

            if (sent > 0) {
                cartService.clearCart(userSession.getUserId());
                loadCart();
                addSuccess(sent + " request(s) sent successfully! Check 'My Requests' to track status.");
            }

        } catch (Exception e) {
            addError("Failed to send requests. Please try again.");
            e.printStackTrace();
        }
    }

    // ── CLEAR CART ────────────────────────────────────────

    public void clearCart() {
        try {
            cartService.clearCart(userSession.getUserId());
            addSuccess("Cart cleared.");
            loadCart();
        } catch (Exception e) {
            addError("Failed to clear cart.");
        }
    }

    // ── NAVIGATE ──────────────────────────────────────────

    public String goToCart() {
        return "/customer/cart.xhtml?faces-redirect=true";
    }

    // ── TOTALS ────────────────────────────────────────────

    public BigDecimal getItemSubtotal(Cart item) {
        if (item.getProductId() == null || item.getProductId().getPrice() == null)
            return BigDecimal.ZERO;
        return item.getProductId().getPrice()
                   .multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    public BigDecimal getGrandTotal() {
        if (cartItems == null) return BigDecimal.ZERO;
        return cartItems.stream()
                .map(this::getItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        if (cartItems == null) return 0;
        return cartItems.stream().mapToInt(Cart::getQuantity).sum();
    }

    // ── HELPERS ───────────────────────────────────────────

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public List<Cart> getCartItems()                  { return cartItems; }
    public boolean    isCartEmpty()                   { return cartItems == null || cartItems.isEmpty(); }
    public int        getCartCount()                  { return cartItems == null ? 0 : cartItems.size(); }

    public int  getAddQuantity()                      { return addQuantity; }
    public void setAddQuantity(int addQuantity)       { this.addQuantity = addQuantity; }
}
