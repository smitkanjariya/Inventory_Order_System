package ejb;

import entity.Payments;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface AdminPaymentServiceLocal {
    List<Payments> getAllPayments();
    void updateStatus(int paymentId, Payments.Status status);
}
