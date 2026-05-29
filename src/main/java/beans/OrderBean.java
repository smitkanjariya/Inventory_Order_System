package beans;

import ejb.OrderServiceLocal;
import entity.OrderItems;
import entity.Orders;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("orderBean")
@ViewScoped
public class OrderBean implements Serializable {

    @EJB
    private OrderServiceLocal orderService;

    private List<Orders> orderList;
    private List<OrderItems> selectedOrderItems;
    private Orders selectedOrder;

    // For delete confirmation
    private Integer deleteOrderId;

    @PostConstruct
    public void init() {
        loadOrders();
    }

    private void loadOrders() {
        orderList = orderService.getAllOrders();
    }

    // View order items detail
    public void viewOrderItems(Orders order) {
        this.selectedOrder = order;
        this.selectedOrderItems = orderService.getOrderItems(order.getOrderId());
    }

    public void closeDetail() {
        this.selectedOrder = null;
        this.selectedOrderItems = null;
    }

    // Approve order
    public void approveOrder(Orders order) {
        try {
            if (!"Pending".equalsIgnoreCase(order.getStatus())) {
                addError("Only Pending orders can be approved!");
                return;
            }
            orderService.updateOrderStatus(order.getOrderId(), "Approved");
            addSuccess("Order #" + order.getOrderId() + " approved successfully!");
            loadOrders();
        } catch (Exception e) {
            addError("Failed to approve order!");
        }
    }

    // Reject order
    public void rejectOrder(Orders order) {
        try {
            if (!"Pending".equalsIgnoreCase(order.getStatus())) {
                addError("Only Pending orders can be rejected!");
                return;
            }
            orderService.updateOrderStatus(order.getOrderId(), "Rejected");
            addSuccess("Order #" + order.getOrderId() + " rejected!");
            loadOrders();
        } catch (Exception e) {
            addError("Failed to reject order!");
        }
    }

    // Complete order
    public void completeOrder(Orders order) {
        try {
            if (!"Approved".equalsIgnoreCase(order.getStatus())) {
                addError("Only Approved orders can be marked as Completed!");
                return;
            }
            orderService.updateOrderStatus(order.getOrderId(), "Completed");
            addSuccess("Order #" + order.getOrderId() + " marked as Completed!");
            loadOrders();
        } catch (Exception e) {
            addError("Failed to complete order!");
        }
    }

    // Cancel order
    public void prepareCancel(Integer orderId) {
        this.deleteOrderId = orderId;
    }

    public void confirmCancel() {
        if (deleteOrderId != null) {
            try {
                Orders order = orderService.getOrderById(deleteOrderId);
                if ("Completed".equalsIgnoreCase(order.getStatus())) {
                    addError("Completed orders cannot be cancelled!");
                    deleteOrderId = null;
                    return;
                }
                orderService.cancelOrder(deleteOrderId);
                addSuccess("Order #" + deleteOrderId + " cancelled!");
                loadOrders();
                deleteOrderId = null;
            } catch (Exception e) {
                addError("Failed to cancel order!");
            }
        }
    }

    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // Getters & Setters
    public List<Orders>    getOrderList()          { return orderList; }
    public List<OrderItems> getSelectedOrderItems() { return selectedOrderItems; }
    public Orders          getSelectedOrder()       { return selectedOrder; }
    public Integer         getDeleteOrderId()       { return deleteOrderId; }
    public void            setDeleteOrderId(Integer id) { this.deleteOrderId = id; }

    public long getPendingCount() {
        return orderList == null ? 0 : orderList.stream()
                .filter(o -> "Pending".equalsIgnoreCase(o.getStatus())).count();
    }
    public long getApprovedCount() {
        return orderList == null ? 0 : orderList.stream()
                .filter(o -> "Approved".equalsIgnoreCase(o.getStatus())).count();
    }
    public long getCompletedCount() {
        return orderList == null ? 0 : orderList.stream()
                .filter(o -> "Completed".equalsIgnoreCase(o.getStatus())).count();
    }
}
