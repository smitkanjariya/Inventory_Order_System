package ejb;

import entity.OrderItems;
import entity.Orders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class OrderService implements OrderServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    public void placeOrder(Orders order, List<OrderItems> items) {
        em.persist(order);
        for (OrderItems item : items) {
            item.setOrderId(order);
            em.persist(item);
        }
    }

    public List<Orders> getAllOrders() {
        return em.createNamedQuery("Orders.findAll", Orders.class).getResultList();
    }

    public List<Orders> getOrdersByUser(int userId) {
        return em.createQuery(
                "SELECT o FROM Orders o WHERE o.userId.userId = :userId", Orders.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Orders getOrderById(int orderId) {
        return em.find(Orders.class, orderId);
    }

    public void updateOrderStatus(int orderId, String status) {
        Orders order = em.find(Orders.class, orderId);
        if (order != null) {
            order.setStatus(status);
            em.merge(order);
        }
    }

    public void cancelOrder(int orderId) {
        updateOrderStatus(orderId, "Cancelled");
    }

    public List<OrderItems> getOrderItems(int orderId) {
        return em.createQuery(
                "SELECT i FROM OrderItems i WHERE i.orderId.orderId = :orderId", OrderItems.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public long getOrderCount() {
        return (long) em.createQuery("SELECT COUNT(o) FROM Orders o").getSingleResult();
    }

    public double getTotalRevenue() {
        Object result = em.createQuery(
                "SELECT SUM(o.totalAmount) FROM Orders o WHERE o.status = 'Completed'")
                .getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public List<Orders> getRecentOrders() {
        return em.createQuery(
                "SELECT o FROM Orders o ORDER BY o.orderDate DESC", Orders.class)
                .setMaxResults(5)
                .getResultList();
    }
}
