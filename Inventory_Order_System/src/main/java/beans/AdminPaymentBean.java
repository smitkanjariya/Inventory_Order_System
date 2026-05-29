package beans;

import entity.Payments;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.List;
import ejb.AdminPaymentServiceLocal;

@Named("adminPaymentBean")
@RequestScoped
public class AdminPaymentBean {

    @EJB
    private AdminPaymentServiceLocal adminPaymentService;

    private List<Payments> paymentList;

    @PostConstruct
    public void init() {
        paymentList = adminPaymentService.getAllPayments();
    }

    public void updateStatus(Payments payment, String status) {
        try {
            adminPaymentService.updateStatus(payment.getPaymentId(), Payments.Status.valueOf(status));
            paymentList = adminPaymentService.getAllPayments();
            addSuccess("Payment #" + payment.getPaymentId() + " marked as " + status);
        } catch (Exception e) {
            addError("Failed to update payment status.");
        }
    }

    public long getPaidCount() {
        if (paymentList == null) return 0;
        return paymentList.stream().filter(p -> p.getStatus() == Payments.Status.COMPLETED).count();
    }

    public long getPendingCount() {
        if (paymentList == null) return 0;
        return paymentList.stream().filter(p -> p.getStatus() == Payments.Status.PENDING).count();
    }

    public long getFailedCount() {
        if (paymentList == null) return 0;
        return paymentList.stream().filter(p -> p.getStatus() == Payments.Status.FAILED).count();
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    public List<Payments> getPaymentList() { return paymentList; }
}
