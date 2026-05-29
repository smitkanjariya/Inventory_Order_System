package ejb;

import entity.Payments;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class AdminPaymentService implements AdminPaymentServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    @Override
    public List<Payments> getAllPayments() {
        return em.createNamedQuery("Payments.findAll", Payments.class).getResultList();
    }

    @Override
    public void updateStatus(int paymentId, Payments.Status status) {
        Payments p = em.find(Payments.class, paymentId);
        if (p != null) {
            p.setStatus(status);
            em.merge(p);
        }
    }
}
