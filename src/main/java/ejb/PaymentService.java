package ejb;

import entity.Payments;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class PaymentService implements PaymentServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    @Override
    public void savePayment(Payments payment) {
        em.persist(payment);
        em.flush(); // ensure payment_id is generated before caller reads it
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveAllPayments(List<Payments> payments) {
        for (Payments p : payments) {
            em.persist(p);
        }
    }

    @Override
    public List<Payments> getPaymentsByCustomer(int customerId) {
        return em.createNamedQuery("Payments.findByCustomer", Payments.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public List<Payments> getPaymentsByCustomerAndManager(int customerId, int managerId) {
        return em.createQuery(
                "SELECT p FROM Payments p WHERE p.customerId.userId = :customerId" +
                " AND p.managerId.userId = :managerId ORDER BY p.paymentDate DESC",
                Payments.class)
                .setParameter("customerId", customerId)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    @Override
    public List<Payments> getPaymentsByManager(int managerId) {
        return em.createQuery(
                "SELECT p FROM Payments p WHERE p.managerId.userId = :managerId ORDER BY p.paymentDate DESC",
                Payments.class)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    @Override
    public boolean hasPaymentForRequests(int customerId, int managerId) {
        Long count = (Long) em.createQuery(
                "SELECT COUNT(p) FROM Payments p WHERE p.customerId.userId = :customerId" +
                " AND p.managerId.userId = :managerId AND p.status = :status")
                .setParameter("customerId", customerId)
                .setParameter("managerId", managerId)
                .setParameter("status", Payments.Status.COMPLETED)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean hasPayment(int customerId, int managerId) {
        Long count = (Long) em.createQuery(
                "SELECT COUNT(p) FROM Payments p WHERE p.customerId.userId = :customerId" +
                " AND p.managerId.userId = :managerId AND p.status = :status")
                .setParameter("customerId", customerId)
                .setParameter("managerId", managerId)
                .setParameter("status", Payments.Status.COMPLETED)
                .getSingleResult();
        return count != null && count > 0;
    }
}
