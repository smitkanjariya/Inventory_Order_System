package beans;

import ejb.UserServiceLocal;
import ejb.ProductServiceLocal;
import ejb.OrderServiceLocal;
import ejb.PaymentServiceLocal;
import ejb.ManagerServiceLocal;
import entity.Users;
import entity.Products;
import entity.Orders;
import entity.Payments;
import entity.Request;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named("adminManagerReportBean")
@ViewScoped
public class AdminManagerReportBean implements Serializable {

    @EJB private UserServiceLocal userService;
    @EJB private ProductServiceLocal productService;
    @EJB private OrderServiceLocal orderService;
    @EJB private PaymentServiceLocal paymentService;
    @EJB private ManagerServiceLocal managerService;

    private List<ManagerReport> managerReports;
    private ManagerReport selectedManagerReport;

    public static class ManagerReport implements Serializable {
        private final Users manager;
        private final long totalProducts;
        private final long totalOrders;
        private final BigDecimal totalRevenue;
        private final long totalPayments;
        private final List<Products> products;
        private final List<String> categories;

        public ManagerReport(Users manager, long totalProducts, long totalOrders,
                           BigDecimal totalRevenue, long totalPayments,
                           List<Products> products, List<String> categories) {
            this.manager = manager;
            this.totalProducts = totalProducts;
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.totalPayments = totalPayments;
            this.products = products;
            this.categories = categories;
        }

        public Users getManager() { return manager; }
        public long getTotalProducts() { return totalProducts; }
        public long getTotalOrders() { return totalOrders; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public long getTotalPayments() { return totalPayments; }
        public List<Products> getProducts() { return products; }
        public List<String> getCategories() { return categories; }
    }

    @PostConstruct
    public void init() {
        loadManagerReports();
    }

    public void loadManagerReports() {
        managerReports = new ArrayList<>();
        
        List<Users> managers = userService.getUsersByRole("Manager");
        
        for (Users manager : managers) {
            List<Products> managerProducts = productService.getProductsByManager(manager.getUserId());
            
            List<Payments> managerPayments = paymentService.getPaymentsByManager(manager.getUserId())
                    .stream()
                    .filter(p -> p.getStatus() == Payments.Status.COMPLETED)
                    .collect(Collectors.toList());
            
            List<Request> managerRequests = managerService.getRequestsByManager(manager.getUserId())
                    .stream()
                    .filter(r -> r.getStatus() == Request.Status.PAID)
                    .collect(Collectors.toList());
            
            BigDecimal totalRevenue = managerPayments.stream()
                    .map(Payments::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Set<String> categorySet = managerProducts.stream()
                    .map(p -> p.getCategoryId() != null ? p.getCategoryId().getCategoryName() : "Uncategorized")
                    .collect(Collectors.toSet());
            
            ManagerReport report = new ManagerReport(
                    manager,
                    managerProducts.size(),
                    managerRequests.size(),
                    totalRevenue,
                    managerPayments.size(),
                    managerProducts,
                    new ArrayList<>(categorySet)
            );
            
            managerReports.add(report);
        }
        
        managerReports.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));
    }

    public void selectManager(ManagerReport report) {
        this.selectedManagerReport = report;
    }

    public List<ManagerReport> getManagerReports() { return managerReports; }
    public ManagerReport getSelectedManagerReport() { return selectedManagerReport; }
    public void setSelectedManagerReport(ManagerReport report) { this.selectedManagerReport = report; }
}
