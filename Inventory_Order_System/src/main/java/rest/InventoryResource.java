package rest;

import ejb.InventoryServiceLocal;
import ejb.ProductServiceLocal;
import entity.Inventory;
import entity.Products;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @EJB
    private InventoryServiceLocal inventoryService;

    @EJB
    private ProductServiceLocal productService;

    // GET /api/inventory
    @GET
    public Response getAllInventory() {
        try {
            List<Inventory> list = inventoryService.getAllInventory();
            List<InventoryDTO> result = list.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch inventory\"}").build();
        }
    }

    // GET /api/inventory/product/{productId}
    @GET
    @Path("/product/{productId}")
    public Response getInventoryByProduct(@PathParam("productId") int productId) {
        try {
            Inventory inv = inventoryService.getInventoryByProduct(productId);
            if (inv == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Inventory record not found\"}").build();
            }
            return Response.ok(toDTO(inv)).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch inventory\"}").build();
        }
    }

    // GET /api/inventory/lowstock
    @GET
    @Path("/lowstock")
    public Response getLowStockAlert() {
        try {
            List<Products> lowStock = productService.getLowStockProducts();
            List<LowStockDTO> result = lowStock.stream()
                    .map(p -> {
                        LowStockDTO dto = new LowStockDTO();
                        dto.productId    = p.getProductId();
                        dto.productName  = p.getName();
                        dto.quantity     = p.getQuantity();
                        dto.reorderLevel = p.getReorderLevel();
                        dto.category     = p.getCategoryId() != null
                                ? p.getCategoryId().getCategoryName() : null;
                        return dto;
                    })
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch low stock alert\"}").build();
        }
    }

    private InventoryDTO toDTO(Inventory i) {
        InventoryDTO dto = new InventoryDTO();
        dto.inventoryId  = i.getInventoryId();
        dto.productId    = i.getProductId() != null ? i.getProductId().getProductId() : null;
        dto.productName  = i.getProductId() != null ? i.getProductId().getName() : null;
        dto.stockIn      = i.getStockIn() != null ? i.getStockIn() : 0;
        dto.stockOut     = i.getStockOut() != null ? i.getStockOut() : 0;
        dto.lastUpdated  = i.getLastUpdated() != null ? i.getLastUpdated().toString() : null;
        return dto;
    }

    public static class InventoryDTO {
        public int inventoryId;
        public Integer productId;
        public String productName;
        public int stockIn;
        public int stockOut;
        public String lastUpdated;
    }

    public static class LowStockDTO {
        public int productId;
        public String productName;
        public int quantity;
        public int reorderLevel;
        public String category;
    }
}
