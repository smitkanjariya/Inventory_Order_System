package beans;

import com.razorpay.Utils;
import ejb.ManagerServiceLocal;
import ejb.PaymentServiceLocal;
import ejb.ReportServiceLocal;
import ejb.UserServiceLocal;
import entity.Payments;
import entity.Reports;
import entity.Request;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import session.UserSession;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named("customerRequestBean")
@ViewScoped
public class CustomerRequestBean implements Serializable {

    private static final String RZP_KEY_ID     = "rzp_test_SXr0vlkLDQmIKb";
    private static final String RZP_KEY_SECRET = "3Y3bau3Byzq7eocD2i8UQGwH";

    @EJB private ManagerServiceLocal managerService;
    @EJB private PaymentServiceLocal paymentService;
    @EJB private ReportServiceLocal  reportService;
    @EJB private UserServiceLocal    userService;
    @Inject private UserSession      userSession;

    private List<Request>             myRequests;
    private List<ManagerRequestGroup> acceptedGroups;
    private List<Payments>            allPayments;

    // requestId -> Payment (for per-request receipt lookup in table)
    private Map<Integer, Payments> requestPaymentMap = new HashMap<>();

    // Razorpay state
    private Integer   pendingManagerId;
    private Integer   pendingRequestId;   // null = group payment, non-null = per-request
    private BigDecimal pendingAmount;

    private String rzpPaymentId;
    private String rzpSignature;
    private String rzpOrderIdCallback;

    // ── inner class ───────────────────────────────────────────────────────────
    public static class ManagerRequestGroup implements Serializable {
        private final Users          manager;
        private final List<Request>  requests;
        private final BigDecimal     total;
        private       BigDecimal     paidSoFar;
        private       BigDecimal     remaining;
        private       boolean        paid;
        private       List<Payments> receipts;

        public ManagerRequestGroup(Users manager, List<Request> requests,
                                   BigDecimal total, BigDecimal paidSoFar,
                                   List<Payments> receipts) {
            this.manager   = manager;
            this.requests  = requests;
            this.total     = total;
            this.paidSoFar = paidSoFar;
            this.remaining = total.subtract(paidSoFar).max(BigDecimal.ZERO);
            this.paid      = this.remaining.compareTo(BigDecimal.ZERO) == 0;
            this.receipts  = receipts;
        }

        public Users          getManager()               { return manager; }
        public List<Request>  getRequests()              { return requests; }
        public BigDecimal     getTotal()                 { return total; }
        public BigDecimal     getPaidSoFar()             { return paidSoFar; }
        public BigDecimal     getRemaining()             { return remaining; }
        public boolean        isPaid()                   { return paid; }
        public void           setPaid(boolean v)         { this.paid = v; }
        public void           setRemaining(BigDecimal v) { this.remaining = v; }
        public List<Payments> getReceipts()              { return receipts; }
        public void           setReceipts(List<Payments> v) { this.receipts = v; }
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────
    @PostConstruct
    public void init() {
        try {
            reload();
        } catch (Exception e) {
            e.printStackTrace();
            // fallback: load only requests, skip payment map
            try { myRequests = managerService.getRequestsByCustomer(userSession.getUserId()); } catch (Exception ignored) {}
            if (myRequests == null) myRequests = Collections.emptyList();
            if (acceptedGroups == null) acceptedGroups = Collections.emptyList();
            if (allPayments == null) allPayments = Collections.emptyList();
        }
    }

    public void reload() {
        myRequests = managerService.getRequestsByCustomer(userSession.getUserId());
        try { autoMarkOldPaidRequests(); } catch (Exception ignored) {}
        myRequests = managerService.getRequestsByCustomer(userSession.getUserId());
        buildAcceptedGroups();
        allPayments = paymentService.getPaymentsByCustomer(userSession.getUserId());
        buildRequestPaymentMap();
    }

    // ── build per-request payment map ────────────────────────────────────────
    private void buildRequestPaymentMap() {
        requestPaymentMap = new HashMap<>();
        if (allPayments == null) return;
        for (Payments p : allPayments) {
            if (p.getRequestId() != null) {
                requestPaymentMap.put(p.getRequestId(), p);
            }
        }
    }

    // ── build accepted groups (manager-level) ────────────────────────────────
    private void buildAcceptedGroups() {
        int customerId = userSession.getUserId();
        List<Request> accepted = myRequests == null ? Collections.emptyList() :
                myRequests.stream()
                          .filter(r -> r.getStatus() == Request.Status.ACCEPTED)
                          .collect(Collectors.toList());

        Map<Integer, List<Request>> byManager = new LinkedHashMap<>();
        for (Request r : accepted) {
            byManager.computeIfAbsent(r.getManagerId().getUserId(), k -> new ArrayList<>()).add(r);
        }

        acceptedGroups = new ArrayList<>();
        for (Map.Entry<Integer, List<Request>> entry : byManager.entrySet()) {
            List<Request> reqs    = entry.getValue();
            Users         manager = reqs.get(0).getManagerId();
            BigDecimal    total   = reqs.stream()
                    .map(r -> r.getProductId().getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            List<Payments> receipts = paymentService.getPaymentsByCustomerAndManager(customerId, manager.getUserId());
            BigDecimal paidSoFar = receipts.stream()
                    .filter(p -> p.getStatus() == Payments.Status.COMPLETED)
                    .map(Payments::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            acceptedGroups.add(new ManagerRequestGroup(manager, reqs, total, paidSoFar, receipts));
        }
    }

    // ── auto-mark legacy paid requests ───────────────────────────────────────
    private void autoMarkOldPaidRequests() {
        int customerId = userSession.getUserId();
        if (myRequests == null) return;
        Map<Integer, List<Request>> byManager = new LinkedHashMap<>();
        for (Request r : myRequests) {
            if (r.getStatus() == Request.Status.ACCEPTED)
                byManager.computeIfAbsent(r.getManagerId().getUserId(), k -> new ArrayList<>()).add(r);
        }
        for (Map.Entry<Integer, List<Request>> entry : byManager.entrySet()) {
            List<Request> reqs    = entry.getValue();
            Users         manager = reqs.get(0).getManagerId();
            BigDecimal    total   = reqs.stream()
                    .map(r -> r.getProductId().getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paidSoFar = paymentService
                    .getPaymentsByCustomerAndManager(customerId, manager.getUserId()).stream()
                    .filter(p -> p.getStatus() == Payments.Status.COMPLETED)
                    .map(Payments::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (paidSoFar.compareTo(total) >= 0)
                reqs.forEach(r -> managerService.updateRequestStatus(r.getRequestId(), Request.Status.PAID));
        }
    }

    // ── Razorpay confirmation (handles both per-request and group) ────────────
    public void confirmRazorpayPayment() {
        try {
            if (rzpPaymentId == null || rzpSignature == null || rzpOrderIdCallback == null) {
                addMsg(FacesMessage.SEVERITY_ERROR, "Payment verification failed: missing data.");
                return;
            }

            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id",   rzpOrderIdCallback);
            attributes.put("razorpay_payment_id", rzpPaymentId);
            attributes.put("razorpay_signature",  rzpSignature);
            boolean valid = Utils.verifyPaymentSignature(attributes, RZP_KEY_SECRET);
            if (!valid) { addMsg(FacesMessage.SEVERITY_ERROR, "Payment signature verification failed!"); return; }

            Users customer = userService.getUserById(userSession.getUserId());

            if (pendingRequestId != null) {
                // ── Per-request payment ──────────────────────────────────────
                Request req = myRequests == null ? null : myRequests.stream()
                        .filter(r -> r.getRequestId().equals(pendingRequestId))
                        .findFirst().orElse(null);
                if (req == null) { addMsg(FacesMessage.SEVERITY_ERROR, "Request not found."); return; }

                BigDecimal amount = req.getProductId().getPrice()
                        .multiply(BigDecimal.valueOf(req.getQuantity()));

                Payments p = buildPayment(customer, req.getManagerId(), amount);
                p.setRequestId(pendingRequestId);
                paymentService.savePayment(p);
                managerService.updateRequestStatus(pendingRequestId, Request.Status.PAID);
                saveReports(customer, req.getManagerId(), amount, p.getPaymentId());

                addMsg(FacesMessage.SEVERITY_INFO,
                        "Payment of \u20b9" + amount.toPlainString()
                        + " for request #" + pendingRequestId + " successful via Razorpay!");

            } else {
                // ── Group payment (manager-level) ────────────────────────────
                ManagerRequestGroup group = acceptedGroups == null ? null : acceptedGroups.stream()
                        .filter(g -> g.getManager().getUserId().equals(pendingManagerId))
                        .findFirst().orElse(null);
                if (group == null) { addMsg(FacesMessage.SEVERITY_ERROR, "Payment group not found."); return; }

                Payments p = buildPayment(customer, group.getManager(), group.getRemaining());
                paymentService.savePayment(p);
                group.getRequests().forEach(r -> managerService.updateRequestStatus(r.getRequestId(), Request.Status.PAID));
                saveReports(customer, group.getManager(), group.getRemaining(), p.getPaymentId());

                group.setPaid(true);
                group.setRemaining(BigDecimal.ZERO);
                group.setReceipts(paymentService.getPaymentsByCustomerAndManager(
                        userSession.getUserId(), group.getManager().getUserId()));

                addMsg(FacesMessage.SEVERITY_INFO,
                        "Payment of \u20b9" + p.getTotalAmount().toPlainString()
                        + " to " + group.getManager().getOrganizationName() + " successful via Razorpay!");
            }

            // reset state
            rzpPaymentId = null; rzpSignature = null;
            rzpOrderIdCallback = null; pendingManagerId = null;
            pendingRequestId = null; pendingAmount = null;

            // refresh
            myRequests  = managerService.getRequestsByCustomer(userSession.getUserId());
            allPayments = paymentService.getPaymentsByCustomer(userSession.getUserId());
            buildRequestPaymentMap();
            buildAcceptedGroups();

        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Payment confirmation failed. Contact support.");
            e.printStackTrace();
        }
    }

    private Payments buildPayment(Users customer, Users manager, BigDecimal amount) {
        Payments p = new Payments();
        p.setCustomerId(customer);
        p.setManagerId(manager);
        p.setTotalAmount(amount);
        p.setStatus(Payments.Status.COMPLETED);
        p.setPaymentDate(new Date());
        p.setPaymentMethod("Razorpay");
        p.setRazorpayPaymentId(rzpPaymentId);
        p.setRazorpayOrderId(rzpOrderIdCallback);
        return p;
    }

    private void saveReports(Users customer, Users manager, BigDecimal amount, Integer paymentId) {
        Reports pre = new Reports();
        pre.setReportType("Payment Pending");
        pre.setCustomerId(customer); pre.setManagerId(manager);
        pre.setTotalAmount(amount);  pre.setPaymentStatus("PENDING");
        pre.setGeneratedDate(new Date());
        reportService.saveReport(pre);

        Reports post = new Reports();
        post.setReportType("Payment Completed");
        post.setCustomerId(customer); post.setManagerId(manager);
        post.setTotalAmount(amount);  post.setPaymentStatus("COMPLETED");
        post.setPaymentId(paymentId); post.setGeneratedDate(new Date());
        reportService.saveReport(post);
    }

    // ── helpers for table row ─────────────────────────────────────────────────
    public Payments getPaymentForRequest(Integer requestId) {
        return requestPaymentMap.get(requestId);
    }

    public boolean isRequestPaid(Integer requestId) {
        return requestPaymentMap.containsKey(requestId);
    }

    private void addMsg(FacesMessage.Severity sev, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, msg, null));
    }

    // ── counts ────────────────────────────────────────────────────────────────
    public long getPendingCount()  { return myRequests == null ? 0 : myRequests.stream().filter(r -> r.getStatus() == Request.Status.PENDING).count(); }
    public long getAcceptedCount() { return myRequests == null ? 0 : myRequests.stream().filter(r -> r.getStatus() == Request.Status.ACCEPTED).count(); }
    public long getRejectedCount() { return myRequests == null ? 0 : myRequests.stream().filter(r -> r.getStatus() == Request.Status.REJECTED).count(); }
    public long getPaidCount()     { return myRequests == null ? 0 : myRequests.stream().filter(r -> r.getStatus() == Request.Status.PAID).count(); }

    // ── getters / setters ─────────────────────────────────────────────────────
    public List<Request>             getMyRequests()        { return myRequests; }
    public List<ManagerRequestGroup> getAcceptedGroups()    { return acceptedGroups; }
    public List<Payments>            getAllPayments()        { return allPayments; }
    public Map<Integer, Payments>    getRequestPaymentMap() { return requestPaymentMap; }

    public ManagerRequestGroup getGroupByManager(Integer managerId) {
        if (acceptedGroups == null || managerId == null) return null;
        return acceptedGroups.stream()
                .filter(g -> g.getManager().getUserId().equals(managerId))
                .findFirst().orElse(null);
    }

    public String     getRzpKeyId()                { return RZP_KEY_ID; }
    public String     getRzpPaymentId()            { return rzpPaymentId; }
    public void       setRzpPaymentId(String v)    { this.rzpPaymentId = v; }
    public String     getRzpSignature()            { return rzpSignature; }
    public void       setRzpSignature(String v)    { this.rzpSignature = v; }
    public String     getRzpOrderIdCallback()      { return rzpOrderIdCallback; }
    public void       setRzpOrderIdCallback(String v) { this.rzpOrderIdCallback = v; }
    public Integer    getPendingManagerId()        { return pendingManagerId; }
    public void       setPendingManagerId(Integer v)  { this.pendingManagerId = v; }
    public Integer    getPendingRequestId()        { return pendingRequestId; }
    public void       setPendingRequestId(Integer v)  { this.pendingRequestId = v; }
    public BigDecimal getPendingAmount()           { return pendingAmount; }
    public void       setPendingAmount(BigDecimal v)  { this.pendingAmount = v; }
}
