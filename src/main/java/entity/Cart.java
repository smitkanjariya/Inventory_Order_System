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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "cart")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Cart.findAll",            query = "SELECT c FROM Cart c"),
    @NamedQuery(name = "Cart.findByCustomer",      query = "SELECT c FROM Cart c WHERE c.customerId.userId = :customerId"),
    @NamedQuery(name = "Cart.findByCustomerProduct",
                query = "SELECT c FROM Cart c WHERE c.customerId.userId = :customerId AND c.productId.productId = :productId")
})
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;

    @NotNull
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private Users customerId;

    @NotNull
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @ManyToOne
    private Products productId;

    @NotNull
    @Column(name = "quantity")
    private int quantity;

    public Cart() {}

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public Users getCustomerId() { return customerId; }
    public void setCustomerId(Users customerId) { this.customerId = customerId; }

    public Products getProductId() { return productId; }
    public void setProductId(Products productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public int hashCode() {
        return (cartId != null ? cartId.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Cart)) return false;
        Cart other = (Cart) object;
        return !((this.cartId == null && other.cartId != null) ||
                 (this.cartId != null && !this.cartId.equals(other.cartId)));
    }

    @Override
    public String toString() {
        return "entity.Cart[ cartId=" + cartId + " ]";
    }
}
