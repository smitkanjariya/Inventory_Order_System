package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "reports")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Reports.findAll",           query = "SELECT r FROM Reports r ORDER BY r.generatedDate DESC"),
    @NamedQuery(name = "Reports.findByReportType",  query = "SELECT r FROM Reports r WHERE r.reportType = :reportType ORDER BY r.generatedDate DESC"),
    @NamedQuery(name = "Reports.findByCustomer",    query = "SELECT r FROM Reports r WHERE r.customerId.userId = :customerId ORDER BY r.generatedDate DESC"),
    @NamedQuery(name = "Reports.findByManager",     query = "SELECT r FROM Reports r WHERE r.managerId.userId = :managerId ORDER BY r.generatedDate DESC")
})
public class Reports implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "report_type", length = 50)
    private String reportType;

    @Column(name = "generated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date generatedDate;

    @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    @ManyToOne
    private Users customerId;

    @JoinColumn(name = "manager_id", referencedColumnName = "user_id")
    @ManyToOne
    private Users managerId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "payment_id")
    private Integer paymentId;

    public Reports() {}

    // ── getters / setters ──────────────────────────────
    public Integer    getReportId()                        { return reportId; }
    public void       setReportId(Integer v)               { this.reportId = v; }

    public String     getReportType()                      { return reportType; }
    public void       setReportType(String v)              { this.reportType = v; }

    public Date       getGeneratedDate()                   { return generatedDate; }
    public void       setGeneratedDate(Date v)             { this.generatedDate = v; }

    public Users      getCustomerId()                      { return customerId; }
    public void       setCustomerId(Users v)               { this.customerId = v; }

    public Users      getManagerId()                       { return managerId; }
    public void       setManagerId(Users v)                { this.managerId = v; }

    public BigDecimal getTotalAmount()                     { return totalAmount; }
    public void       setTotalAmount(BigDecimal v)         { this.totalAmount = v; }

    public String     getPaymentStatus()                   { return paymentStatus; }
    public void       setPaymentStatus(String v)           { this.paymentStatus = v; }

    public Integer    getPaymentId()                       { return paymentId; }
    public void       setPaymentId(Integer v)              { this.paymentId = v; }

    @Override
    public int hashCode() { return reportId != null ? reportId.hashCode() : 0; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reports)) return false;
        Reports other = (Reports) o;
        return (this.reportId == null && other.reportId == null) ||
               (this.reportId != null && this.reportId.equals(other.reportId));
    }
}
