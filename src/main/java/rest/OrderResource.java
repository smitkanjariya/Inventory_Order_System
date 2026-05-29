package rest;

import ejb.OrderServiceLocal;
import entity.OrderItems;
import entity.Orders;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @EJB
    private OrderServiceLocal orderService;

    // GET /api/orders
    @GET
    public Response getAllOrders() {
        try {
            List<Orders> orders = orderService.getAllOrders();
            List<OrderDTO> result = orders.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch orders\"}").build();
        }
    }

    // GET /api/orders/{id}
    @GET
    @Path("/{id}")
    public Response getOrderById(@PathParam("id") int id) {
        try {
            Orders o = orderService.getOrderById(id);
            if (o == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Order not found\"}").build();
            }
            return Response.ok(toDTO(o)).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch order\"}").build();
        }
    }

    // GET /api/orders/{id}/items
    @GET
    @Path("/{id}/items")
    public Response getOrderItems(@PathParam("id") int id) {
        try {
            List<OrderItems> items = orderService.getOrderItems(id);
            List<OrderItemDTO> result = items.stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch order items\"}").build();
        }
    }

    // GET /api/orders/status?value=Pending
    @GET
    @Path("/status")
    public Response getOrdersByStatus(@QueryParam("value") String status) {
        try {
            List<OrderDTO> result = orderService.getAllOrders().stream()
                    .filter(o -> o.getStatus() != null &&
                            o.getStatus().equalsIgnoreCase(status))
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch orders by status\"}").build();
        }
    }

    // PATCH /api/orders/{id}/approve
    @PATCH
    @Path("/{id}/approve")
    public Response approveOrder(@PathParam("id") int id) {
        try {
            orderService.updateOrderStatus(id, "Approved");
            return Response.ok("{\"message\":\"Order approved\"}").build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to approve order\"}").build();
        }
    }

    // PATCH /api/orders/{id}/cancel
    @PATCH
    @Path("/{id}/cancel")
    public Response cancelOrder(@PathParam("id") int id) {
        try {
            orderService.cancelOrder(id);
            return Response.ok("{\"message\":\"Order cancelled\"}").build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to cancel order\"}").build();
        }
    }

    private OrderDTO toDTO(Orders o) {
        OrderDTO dto = new OrderDTO();
        dto.orderId     = o.getOrderId();
        dto.status      = o.getStatus();
        dto.orderDate   = o.getOrderDate() != null ? o.getOrderDate().toString() : null;
        dto.totalAmount = o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0;
        dto.customerName = o.getUserId() != null ? o.getUserId().getName() : null;
        dto.customerEmail = o.getUserId() != null ? o.getUserId().getEmail() : null;
        return dto;
    }

    private OrderItemDTO toItemDTO(OrderItems i) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.orderItemId  = i.getOrderItemId();
        dto.productName  = i.getProductId() != null ? i.getProductId().getName() : null;
        dto.quantity     = i.getQuantity();
        dto.price        = i.getPrice() != null ? i.getPrice().doubleValue() : 0;
        return dto;
    }

    public static class OrderDTO {
        public int orderId;
        public String status;
        public String orderDate;
        public double totalAmount;
        public String customerName;
        public String customerEmail;
    }

    public static class OrderItemDTO {
        public int orderItemId;
        public String productName;
        public int quantity;
        public double price;
    }
}
