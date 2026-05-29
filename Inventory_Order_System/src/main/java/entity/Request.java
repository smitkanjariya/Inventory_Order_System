package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "request")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Request.findAll",          query = "SELECT r FROM Request r"),
    @NamedQuery(name = "Request.findByCustomer",   query = "SELECT r FROM Request r WHERE r.customerId.userId = :customerId"),
    @NamedQuery(name = "Request.findByManager",    query = "SELECT r FROM Request r WHERE r.managerId.userId = :managerId"),
    @NamedQuery(name = "Request.findByStatus",     query = "SELECT r FROM Request r WHERE r.status = :status")
})
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status { PENDING, ACCEPTED, REJECTED, PAID }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @NotNull
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    @ManyToOne
    private Users customerId;

    @NotNull
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @ManyToOne
    private Products productId;

    @NotNull
    @JoinColumn(name = "manager_id", referencedColumnName = "user_id")
    @ManyToOne
    private Users managerId;

    @NotNull
    @Column(name = "quantity")
    private int quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Request() {}

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Users getCustomerId() { return customerId; }
    public void setCustomerId(Users customerId) { this.customerId = customerId; }

    public Products getProductId() { return productId; }
    public void setProductId(Products productId) { this.productId = productId; }

    public Users getManagerId() { return managerId; }
    public void setManagerId(Users managerId) { this.managerId = managerId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public int hashCode() {
        return (requestId != null ? requestId.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Request)) return false;
        Request other = (Request) object;
        return !((this.requestId == null && other.requestId != null) ||
                 (this.requestId != null && !this.requestId.equals(other.requestId)));
    }

    @Override
    public String toString() {
        return "entity.Request[ requestId=" + requestId + " ]";
    }
}
