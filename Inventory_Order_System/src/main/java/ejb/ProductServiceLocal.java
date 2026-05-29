package ejb;

import entity.Products;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ProductServiceLocal {
    void addProduct(Products product);
    void updateProduct(Products product);
    void deleteProduct(int id);
    List<Products> getAllProducts();
    Products getProductById(int id);
    List<Products> getLowStockProducts();
    List<Products> getProductsByManager(int managerId);
    long getProductCount();
    long getLowStockCount();
    void deductStock(int productId, int quantity);
}
