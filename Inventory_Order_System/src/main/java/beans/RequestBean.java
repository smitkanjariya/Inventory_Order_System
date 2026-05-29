package beans;

import ejb.ManagerServiceLocal;
import ejb.PaymentServiceLocal;
import ejb.ProductServiceLocal;
import entity.Payments;
import entity.Products;
import entity.Request;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named("requestBean")
@ViewScoped
public class RequestBean implements Serializable {

    @EJB private ManagerServiceLocal managerService;
    @EJB private ProductServiceLocal productService;
    @EJB private PaymentServiceLocal paymentService;

    @Inject private ManagerBean managerBean;

    private List<Request>            allRequests;
    private List<CustomerOrderGroup> acceptedGroups;
    private String                   activeFilter = "ALL";

    // ── Inner class: groups accepted requests by customer ──
    public static class CustomerOrderGroup implements Serializable {
        private final Users          customer;
        private final List<Request>  requests;
        private final BigDecimal     total;
        private final boolean        paid;
        private final List<Payments> receipts;

        public CustomerOrderGroup(Users customer, List<Request> requests,
                                  BigDecimal total, boolean paid, List<Payments> receipts) {
            this.customer = customer;
            this.requests = requests;
            this.total    = total;
            this.paid     = paid;
            this.receipts = receipts;
        }

        public Users          getCustomer()  { return customer; }
        public List<Request>  getRequests()  { return requests; }
        public BigDecimal     getTotal()     { return total; }
        public boolean        isPaid()       { return paid; }
        public List<Payments> getReceipts()  { return receipts; }
    }

    @PostConstruct
    public void init() { loadRequests(); }

    private void loadRequests() {
        int managerId = managerBean.getManager().getUserId();
        allRequests = managerService.getRequestsByManager(managerId);
        buildAcceptedGroups(managerId);
    }

    private void buildAcceptedGroups(int managerId) {
        // Only ACCEPTED (not PAID) requests need payment
        List<Request> accepted = allRequests == null ? Collections.emptyList() :
                allRequests.stream()
                           .filter(r -> r.getStatus() == Request.Status.ACCEPTED)
                           .collect(Collectors.toList());

        Map<Integer, List<Request>> byCustomer = new LinkedHashMap<>();
        for (Request r : accepted) {
            byCustomer.computeIfAbsent(r.getCustomerId().getUserId(), k -> new ArrayList<>()).add(r);
        }

        acceptedGroups = new ArrayList<>();
        for (Map.Entry<Integer, List<Request>> entry : byCustomer.entrySet()) {
            List<Request> reqs     = entry.getValue();
            Users         customer = reqs.get(0).getCustomerId();
            BigDecimal    total    = reqs.stream()
                    .map(r -> r.getProductId().getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            List<Payments> receipts = paymentService.getPaymentsByCustomerAndManager(
                    customer.getUserId(), managerId);
            // paid = false always here since PAID requests are excluded from the group
            boolean paid = false;
            acceptedGroups.add(new CustomerOrderGroup(customer, reqs, total, paid, receipts));
        }
    }

    public void acceptRequest(Request r) {
        try {
            if (!isOwnRequest(r)) { addError("Unauthorized action."); return; }
            if (r.getStatus() != Request.Status.PENDING) { addError("Only PENDING requests can be accepted."); return; }
            Products product = productService.getProductById(r.getProductId().getProductId());
            if (product.getStock() < r.getQuantity()) {
                addError("\u26a0 Product not in stock! Available: " + product.getStock()
                        + " units, Requested: " + r.getQuantity() + " units.");
                return;
            }
            product.setStock(product.getStock() - r.getQuantity());
            product.setQuantity(product.getStock());
            productService.updateProduct(product);
            managerService.updateRequestStatus(r.getRequestId(), Request.Status.ACCEPTED);
            managerService.createOrderFromRequest(r);
            addSuccess("Request #" + r.getRequestId() + " accepted. Order placed automatically.");
            loadRequests();
            managerBean.refresh();
        } catch (Exception e) { addError("Failed to accept request."); e.printStackTrace(); }
    }

    public void rejectRequest(Request r) {
        try {
            if (!isOwnRequest(r)) { addError("Unauthorized action."); return; }
            if (r.getStatus() != Request.Status.PENDING) { addError("Only PENDING requests can be rejected."); return; }
            managerService.updateRequestStatus(r.getRequestId(), Request.Status.REJECTED);
            addSuccess("Request #" + r.getRequestId() + " rejected.");
            loadRequests();
            managerBean.refresh();
        } catch (Exception e) { addError("Failed to reject request."); e.printStackTrace(); }
    }

    public List<Request> getFilteredRequests() {
        if (allRequests == null) return List.of();
        if ("ALL".equals(activeFilter)) return allRequests;
        Request.Status status = Request.Status.valueOf(activeFilter);
        return allRequests.stream().filter(r -> r.getStatus() == status).collect(Collectors.toList());
    }

    public void setFilter(String filter) { this.activeFilter = filter; }

    public long getPendingCount()  { return allRequests == null ? 0 : allRequests.stream().filter(r -> r.getStatus() == Request.Status.PENDING).count(); }
    public long getAcceptedCount() { return allRequests == null ? 0 : allRequests.stream().filter(r -> r.getStatus() == Request.Status.ACCEPTED).count(); }
    public long getRejectedCount() { return allRequests == null ? 0 : allRequests.stream().filter(r -> r.getStatus() == Request.Status.REJECTED).count(); }

    private boolean isOwnRequest(Request r) {
        return r.getManagerId() != null &&
               r.getManagerId().getUserId().equals(managerBean.getManager().getUserId());
    }
    private void addSuccess(String msg) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null)); }
    private void addError(String msg)   { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null)); }

    public List<Request>            getAllRequests()    { return allRequests; }
    public List<CustomerOrderGroup> getAcceptedGroups(){ return acceptedGroups; }
    public String                   getActiveFilter()  { return activeFilter; }
}
