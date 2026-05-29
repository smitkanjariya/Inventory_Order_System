package rest;

import ejb.InventoryServiceLocal;
import ejb.OrderServiceLocal;
import ejb.ProductServiceLocal;
import ejb.UserServiceLocal;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {

    @EJB
    private ProductServiceLocal productService;

    @EJB
    private OrderServiceLocal orderService;

    @EJB
    private UserServiceLocal userService;

    @EJB
    private InventoryServiceLocal inventoryService;

    // GET /api/dashboard/stats
    @GET
    @Path("/stats")
    public Response getDashboardStats() {
        try {
            DashboardStatsDTO dto = new DashboardStatsDTO();
            dto.totalProducts  = productService.getProductCount();
            dto.totalOrders    = orderService.getOrderCount();
            dto.totalUsers     = userService.getUserCount();
            dto.lowStockCount  = productService.getLowStockCount();
            dto.totalRevenue   = orderService.getTotalRevenue();
            dto.pendingOrders  = orderService.getAllOrders().stream()
                    .filter(o -> "Pending".equalsIgnoreCase(o.getStatus()))
                    .count();
            dto.approvedOrders = orderService.getAllOrders().stream()
                    .filter(o -> "Approved".equalsIgnoreCase(o.getStatus()))
                    .count();
            dto.completedOrders = orderService.getAllOrders().stream()
                    .filter(o -> "Completed".equalsIgnoreCase(o.getStatus()))
                    .count();
            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch dashboard stats\"}").build();
        }
    }

    // GET /api/dashboard/recentorders
    @GET
    @Path("/recentorders")
    public Response getRecentOrders() {
        try {
            var result = orderService.getRecentOrders().stream()
                    .map(o -> {
                        RecentOrderDTO dto = new RecentOrderDTO();
                        dto.orderId      = o.getOrderId();
                        dto.customerName = o.getUserId() != null ? o.getUserId().getName() : null;
                        dto.status       = o.getStatus();
                        dto.totalAmount  = o.getTotalAmount() != null
                                ? o.getTotalAmount().doubleValue() : 0;
                        dto.orderDate    = o.getOrderDate() != null
                                ? o.getOrderDate().toString() : null;
                        return dto;
                    })
                    .toList();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch recent orders\"}").build();
        }
    }

    public static class DashboardStatsDTO {
        public long totalProducts;
        public long totalOrders;
        public long totalUsers;
        public long lowStockCount;
        public double totalRevenue;
        public long pendingOrders;
        public long approvedOrders;
        public long completedOrders;
    }

    public static class RecentOrderDTO {
        public int orderId;
        public String customerName;
        public String status;
        public double totalAmount;
        public String orderDate;
    }
}
