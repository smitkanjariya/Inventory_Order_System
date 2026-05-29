package ejb;

import entity.Inventory;
import entity.Products;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class InventoryService implements InventoryServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    public void addInventoryRecord(Inventory inventory) {
        em.persist(inventory);
    }

    public void updateStock(int productId, int stockIn, int stockOut) {
        List<Inventory> list = em.createQuery(
                "SELECT i FROM Inventory i WHERE i.productId.productId = :productId", Inventory.class)
                .setParameter("productId", productId)
                .getResultList();
        if (!list.isEmpty()) {
            Inventory inv = list.get(0);
            inv.setStockIn(inv.getStockIn() + stockIn);
            inv.setStockOut(inv.getStockOut() + stockOut);
            em.merge(inv);
        } else {
            Inventory inv = new Inventory();
            inv.setProductId(em.find(Products.class, productId));
            inv.setStockIn(stockIn);
            inv.setStockOut(stockOut);
            em.persist(inv);
        }
    }

    public Inventory getInventoryByProduct(int productId) {
        List<Inventory> list = em.createQuery(
                "SELECT i FROM Inventory i WHERE i.productId.productId = :productId", Inventory.class)
                .setParameter("productId", productId)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Inventory> getAllInventory() {
        return em.createNamedQuery("Inventory.findAll", Inventory.class).getResultList();
    }
}
