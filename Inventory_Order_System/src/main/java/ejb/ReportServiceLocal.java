package ejb;

import entity.Orders;
import entity.Payments;
import entity.Products;
import entity.Reports;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ReportServiceLocal {
    void saveReport(Reports report);
    List<Reports> getReportsByCustomer(int customerId);
    List<Reports> getReportsByManager(int managerId);
    List<Reports> getAllReports();
    List<Orders>   getSalesReport();
    List<Products> getLowStockReport();
    List<Orders>   getOrderReportByStatus(String status);
    List<Payments> getPaymentReport();
}
