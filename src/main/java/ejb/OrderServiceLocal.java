package ejb;

import entity.OrderItems;
import entity.Orders;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface OrderServiceLocal {
    void placeOrder(Orders order, List<OrderItems> items);
    List<Orders> getAllOrders();
    List<Orders> getOrdersByUser(int userId);
    Orders getOrderById(int orderId);
    void updateOrderStatus(int orderId, String status);
    void cancelOrder(int orderId);
    List<OrderItems> getOrderItems(int orderId);
    long getOrderCount();
    double getTotalRevenue();
    List<Orders> getRecentOrders();
}
