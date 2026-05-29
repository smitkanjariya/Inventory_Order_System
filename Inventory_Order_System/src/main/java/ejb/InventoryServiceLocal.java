package ejb;

import entity.Inventory;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface InventoryServiceLocal {
    void addInventoryRecord(Inventory inventory);
    void updateStock(int productId, int stockIn, int stockOut);
    Inventory getInventoryByProduct(int productId);
    List<Inventory> getAllInventory();
}
