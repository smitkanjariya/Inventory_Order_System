package beans;

import ejb.OrderServiceLocal;
import ejb.ProductServiceLocal;
import ejb.UserServiceLocal;
import entity.Orders;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class AdminDashboardBean {

    @EJB
    private UserServiceLocal userService;

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private OrderServiceLocal orderService;

    private long userCount;
    private long productCount;
    private long orderCount;
    private double totalRevenue;
    private List<Orders> recentOrders;

    @PostConstruct
    public void init() {
        userCount = userService.getUserCount();
        productCount = productService.getProductCount();
        orderCount = orderService.getOrderCount();
        totalRevenue = orderService.getTotalRevenue();
        recentOrders = orderService.getRecentOrders();
    }

    public long getUserCount() { return userCount; }
    public long getProductCount() { return productCount; }
    public long getOrderCount() { return orderCount; }
    public double getTotalRevenue() { return totalRevenue; }
    public List<Orders> getRecentOrders() { return recentOrders; }
}
