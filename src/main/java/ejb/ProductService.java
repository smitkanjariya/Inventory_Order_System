package ejb;

import entity.Products;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ProductService implements ProductServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    public void addProduct(Products product) {
        em.persist(product);
    }

    public void updateProduct(Products product) {
        em.merge(product);
    }

    public void deductStock(int productId, int quantity) {
        Products product = em.find(Products.class, productId);
        if (product != null && product.getStock() >= quantity) {
            product.setStock(product.getStock() - quantity);
            em.merge(product);
        }
    }

    public void deleteProduct(int id) {
        Products product = em.find(Products.class, id);
        if (product != null) {
            em.remove(product);
        }
    }

    public List<Products> getAllProducts() {
        return em.createNamedQuery("Products.findAll", Products.class).getResultList();
    }

    public Products getProductById(int id) {
        return em.find(Products.class, id);
    }

    public List<Products> getLowStockProducts() {
        return em.createQuery(
                "SELECT p FROM Products p WHERE p.quantity <= p.reorderLevel", Products.class)
                .getResultList();
    }

    public List<Products> getProductsByManager(int managerId) {
        return em.createQuery(
                "SELECT p FROM Products p WHERE p.managerId.userId = :managerId", Products.class)
                .setParameter("managerId", managerId)
                .getResultList();
    }

    public long getProductCount() {
        return (long) em.createQuery("SELECT COUNT(p) FROM Products p").getSingleResult();
    }

    public long getLowStockCount() {
        return (long) em.createQuery(
                "SELECT COUNT(p) FROM Products p WHERE p.quantity <= p.reorderLevel")
                .getSingleResult();
    }
}
