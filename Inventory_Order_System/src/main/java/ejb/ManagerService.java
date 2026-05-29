package ejb;

import entity.OrderItems;
import entity.Orders;
import entity.Request;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Stateless
public class ManagerService implements ManagerServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    @Override
    public Users findByEmail(String email) {
        try {
            return em.createNamedQuery("Users.findByEmail", Users.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void updateOrganizationName(int userId, String orgName) {
        Users user = em.find(Users.class, userId);
        if (user != null) {
            user.setOrganizationName(orgName);
            em.merge(user);
        }
    }

    @Override
    public long getProductCountByManager(int userId) {
        Object result = em.createQuery(
                "SELECT COUNT(p) FROM Products p WHERE p.managerId.userId = :userId")
                .setParameter("userId", userId)
                .getSingleResult();
        return result != null ? (long) result : 0L;
    }

    @Override
    public long getPendingRequestCount(int userId) {
        Object result = em.createQuery(
                "SELECT COUNT(r) FROM Request r WHERE r.managerId.userId = :userId AND r.status = :status")
                .setParameter("userId", userId)
                .setParameter("status", Request.Status.PENDING)
                .getSingleResult();
        return result != null ? (long) result : 0L;
    }

    @Override
    public List<Request> getRequestsByManager(int userId) {
        return em.createQuery(
                "SELECT r FROM Request r WHERE r.managerId.userId = :userId ORDER BY r.createdAt DESC",
                Request.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<Request> getRequestsByCustomer(int customerId) {
        return em.createQuery(
                "SELECT r FROM Request r WHERE r.customerId.userId = :customerId ORDER BY r.createdAt DESC",
                Request.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public void updateRequestStatus(int requestId, Request.Status status) {
        Request r = em.find(Request.class, requestId);
        if (r != null) {
            r.setStatus(status);
            em.merge(r);
        }
    }

    @Override
    public void saveRequest(Request request) {
        em.persist(request);
    }

    @Override
    public List<Request> getAcceptedRequestsByCustomer(int customerId) {
        return em.createQuery(
                "SELECT r FROM Request r WHERE r.customerId.userId = :customerId AND r.status = :status",
                Request.class)
                .setParameter("customerId", customerId)
                .setParameter("status", Request.Status.ACCEPTED)
                .getResultList();
    }

    @Override
    public Orders createOrderFromRequest(Request r) {
        Orders order = new Orders();
        order.setUserId(r.getCustomerId());
        order.setOrderDate(new Date());
        order.setStatus("Approved");
        BigDecimal total = r.getProductId().getPrice()
                            .multiply(BigDecimal.valueOf(r.getQuantity()));
        order.setTotalAmount(total);
        em.persist(order);

        OrderItems item = new OrderItems();
        item.setOrderId(order);
        item.setProductId(r.getProductId());
        item.setQuantity(r.getQuantity());
        item.setPrice(r.getProductId().getPrice());
        em.persist(item);

        return order;
    }
}
