package ejb;

import entity.Payments;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface PaymentServiceLocal {
    void savePayment(Payments payment);
    void saveAllPayments(List<Payments> payments);
    List<Payments> getPaymentsByCustomer(int customerId);
    List<Payments> getPaymentsByCustomerAndManager(int customerId, int managerId);
    List<Payments> getPaymentsByManager(int managerId);
    boolean hasPayment(int customerId, int managerId);
    boolean hasPaymentForRequests(int customerId, int managerId);
}
