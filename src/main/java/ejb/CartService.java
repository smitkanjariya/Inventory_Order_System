package ejb;

import entity.Cart;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class CartService implements CartServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    @Override
    public void addToCart(Cart cart) {
        em.persist(cart);
    }

    @Override
    public void updateCart(Cart cart) {
        em.merge(cart);
    }

    @Override
    public void removeFromCart(int cartId) {
        Cart cart = em.find(Cart.class, cartId);
        if (cart != null) em.remove(cart);
    }

    @Override
    public void clearCart(int customerId) {
        em.createQuery("DELETE FROM Cart c WHERE c.customerId.userId = :customerId")
                .setParameter("customerId", customerId)
                .executeUpdate();
    }

    @Override
    public List<Cart> getCartByCustomer(int customerId) {
        return em.createNamedQuery("Cart.findByCustomer", Cart.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public Cart getCartItem(int customerId, int productId) {
        try {
            return em.createNamedQuery("Cart.findByCustomerProduct", Cart.class)
                    .setParameter("customerId", customerId)
                    .setParameter("productId", productId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public long getCartItemCount(int customerId) {
        Object result = em.createQuery(
                "SELECT COUNT(c) FROM Cart c WHERE c.customerId.userId = :customerId")
                .setParameter("customerId", customerId)
                .getSingleResult();
        return result != null ? (long) result : 0L;
    }
}
