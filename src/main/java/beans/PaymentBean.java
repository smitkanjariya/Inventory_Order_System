package beans;

import ejb.CartServiceLocal;
import ejb.PaymentServiceLocal;
import ejb.ProductServiceLocal;
import ejb.UserServiceLocal;
import entity.Cart;
import entity.Payments;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import session.UserSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named("paymentBean")
@ViewScoped
public class PaymentBean implements Serializable {

    @EJB private PaymentServiceLocal paymentService;
    @EJB private CartServiceLocal    cartService;
    @EJB private UserServiceLocal    userService;
    @EJB private ProductServiceLocal productService;

    @Inject private UserSession userSession;

    private List<ManagerPaymentGroup> paymentGroups;
    private List<Payments>            paymentHistory;

    public static class ManagerPaymentGroup implements Serializable {

        private final Users      manager;
        private final List<Cart> cartItems;
        private final BigDecimal total;
        private       boolean    alreadyPaid;

        public ManagerPaymentGroup(Users manager, List<Cart> cartItems,
                                   BigDecimal total, boolean alreadyPaid) {
            this.manager    = manager;
            this.cartItems  = cartItems;
            this.total      = total;
            this.alreadyPaid = alreadyPaid;
        }

        public Users     getManager()              { return manager; }
        public List<Cart> getCartItems()           { return cartItems; }
        public BigDecimal getTotal()               { return total; }
        public boolean    isAlreadyPaid()          { return alreadyPaid; }
        public void       setAlreadyPaid(boolean v){ this.alreadyPaid = v; }
    }

    @PostConstruct
    public void init() {
        buildPaymentGroups();
        loadHistory();
    }

    private void buildPaymentGroups() {
        int customerId = userSession.getUserId();
        List<Cart> allItems = cartService.getCartByCustomer(customerId);

        Map<Integer, List<Cart>> byManager = new LinkedHashMap<>();
        for (Cart item : allItems) {
            Users mgr = item.getProductId().getManagerId();
            if (mgr == null) continue;
            byManager.computeIfAbsent(mgr.getUserId(), k -> new ArrayList<>()).add(item);
        }

        paymentGroups = new ArrayList<>();
        for (Map.Entry<Integer, List<Cart>> entry : byManager.entrySet()) {
            List<Cart> items = entry.getValue();
            Users      mgr   = items.get(0).getProductId().getManagerId();

            BigDecimal total = items.stream()
                    .map(c -> c.getProductId().getPrice()
                               .multiply(BigDecimal.valueOf(c.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean paid = paymentService.hasPayment(customerId, mgr.getUserId());
            paymentGroups.add(new ManagerPaymentGroup(mgr, items, total, paid));
        }
    }

    private void loadHistory() {
        paymentHistory = paymentService.getPaymentsByCustomer(userSession.getUserId());
    }

    public void pay(ManagerPaymentGroup group) {
        try {
            if (group.isAlreadyPaid()) {
                addError("Already paid to " + group.getManager().getOrganizationName() + ".");
                return;
            }
            Users customer = userService.getUserById(userSession.getUserId());
            Payments payment = buildPayment(customer, group.getManager(), group.getTotal());
            paymentService.savePayment(payment);

            for (Cart item : group.getCartItems()) {
                productService.deductStock(item.getProductId().getProductId(), item.getQuantity());
            }

            group.setAlreadyPaid(true);
            addSuccess("Payment of \u20b9" + group.getTotal().toPlainString()
                    + " to " + group.getManager().getOrganizationName() + " successful!");
            loadHistory();
        } catch (Exception e) {
            addError("Payment failed. Please try again.");
            e.printStackTrace();
        }
    }

    public void payAll() {
        try {
            List<ManagerPaymentGroup> pending = paymentGroups.stream()
                    .filter(g -> !g.isAlreadyPaid()).toList();

            if (pending.isEmpty()) {
                addError("All payments are already completed.");
                return;
            }

            Users customer = userService.getUserById(userSession.getUserId());
            List<Payments> batch = new ArrayList<>();
            for (ManagerPaymentGroup group : pending) {
                batch.add(buildPayment(customer, group.getManager(), group.getTotal()));
            }

            paymentService.saveAllPayments(batch);

            for (ManagerPaymentGroup group : pending) {
                for (Cart item : group.getCartItems()) {
                    productService.deductStock(item.getProductId().getProductId(), item.getQuantity());
                }
            }

            pending.forEach(g -> g.setAlreadyPaid(true));
            addSuccess(batch.size() + " payment(s) completed. Total: \u20b9" + getUnpaidTotal().toPlainString());
            loadHistory();
        } catch (Exception e) {
            addError("Pay All failed. No payments were saved.");
            e.printStackTrace();
        }
    }

    private Payments buildPayment(Users customer, Users manager, BigDecimal total) {
        Payments p = new Payments();
        p.setCustomerId(customer);
        p.setManagerId(manager);
        p.setTotalAmount(total);
        p.setStatus(Payments.Status.COMPLETED);
        p.setPaymentDate(new Date());
        return p;
    }

    public BigDecimal getGrandTotal() {
        if (paymentGroups == null) return BigDecimal.ZERO;
        return paymentGroups.stream().map(ManagerPaymentGroup::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getUnpaidTotal() {
        if (paymentGroups == null) return BigDecimal.ZERO;
        return paymentGroups.stream().filter(g -> !g.isAlreadyPaid())
                .map(ManagerPaymentGroup::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getPendingPaymentCount() {
        if (paymentGroups == null) return 0;
        return paymentGroups.stream().filter(g -> !g.isAlreadyPaid()).count();
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public List<ManagerPaymentGroup> getPaymentGroups() { return paymentGroups; }
    public List<Payments>            getPaymentHistory() { return paymentHistory; }
}
