package ejb;

import entity.Cart;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CartServiceLocal {
    void addToCart(Cart cart);
    void updateCart(Cart cart);
    void removeFromCart(int cartId);
    void clearCart(int customerId);
    List<Cart> getCartByCustomer(int customerId);
    Cart getCartItem(int customerId, int productId);
    long getCartItemCount(int customerId);
}
