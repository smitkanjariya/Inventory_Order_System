package ejb;

import entity.Orders;
import entity.Payments;
import entity.Products;
import entity.Reports;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ReportService implements ReportServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    @Override
    public void saveReport(Reports report) {
        em.persist(report);
    }

    @Override
    public List<Reports> getReportsByCustomer(int customerId) {
        return em.createNamedQuery("Reports.findByCustomer", Reports.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public List<Reports> getReportsByManager(int managerId) {
        return em.createNamedQuery("Reports.findByManager", Reports.class)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    @Override
    public List<Reports> getAllReports() {
        return em.createNamedQuery("Reports.findAll", Reports.class).getResultList();
    }

    @Override
    public List<Orders> getSalesReport() {
        return em.createNamedQuery("Orders.findAll", Orders.class).getResultList();
    }

    @Override
    public List<Products> getLowStockReport() {
        return em.createQuery(
                "SELECT p FROM Products p WHERE p.quantity <= p.reorderLevel", Products.class)
                .getResultList();
    }

    @Override
    public List<Orders> getOrderReportByStatus(String status) {
        return em.createNamedQuery("Orders.findByStatus", Orders.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Payments> getPaymentReport() {
        return em.createNamedQuery("Payments.findAll", Payments.class).getResultList();
    }
}
