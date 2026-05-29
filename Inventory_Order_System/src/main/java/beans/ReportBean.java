package beans;

import ejb.ReportServiceLocal;
import entity.Orders;
import entity.Payments;
import entity.Products;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;

@Named("reportBean")
@RequestScoped
public class ReportBean {

    @EJB
    private ReportServiceLocal reportService;

    private List<Orders>   salesReport;
    private List<Products>  lowStockReport;
    private List<Payments>  paymentReport;

    // Orders by status filter
    private String selectedStatus = "Pending";
    private List<Orders> ordersByStatus;

    @PostConstruct
    public void init() {
        try { salesReport    = reportService.getSalesReport(); }    catch (Exception e) { salesReport = new java.util.ArrayList<>(); }
        try { lowStockReport = reportService.getLowStockReport(); } catch (Exception e) { lowStockReport = new java.util.ArrayList<>(); }
        try { paymentReport  = reportService.getPaymentReport(); }  catch (Exception e) { paymentReport = new java.util.ArrayList<>(); }
        try { ordersByStatus = reportService.getOrderReportByStatus(selectedStatus); } catch (Exception e) { ordersByStatus = new java.util.ArrayList<>(); }
    }

    public void filterByStatus() {
        ordersByStatus = reportService.getOrderReportByStatus(selectedStatus);
    }

    // Summary counts
    public long getTotalSales() {
        return salesReport == null ? 0 : salesReport.size();
    }

    public double getTotalRevenue() {
        if (salesReport == null) return 0;
        return salesReport.stream()
                .filter(o -> "Completed".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0)
                .sum();
    }

    public long getTotalLowStock() {
        return lowStockReport == null ? 0 : lowStockReport.size();
    }

    public long getTotalPaidPayments() {
        if (paymentReport == null) return 0;
        return paymentReport.stream()
                .filter(p -> p.getStatus() == Payments.Status.COMPLETED).count();
    }

    // Getters & Setters
    public List<Orders>   getSalesReport()    { return salesReport; }
    public List<Products> getLowStockReport() { return lowStockReport; }
    public List<Payments> getPaymentReport()  { return paymentReport; }
    public List<Orders>    getOrdersByStatus()  { return ordersByStatus; }

    public String getSelectedStatus()                    { return selectedStatus; }
    public void   setSelectedStatus(String selectedStatus) { this.selectedStatus = selectedStatus; }
}
