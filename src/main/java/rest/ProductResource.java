package rest;

import ejb.ProductServiceLocal;
import entity.Products;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @EJB
    private ProductServiceLocal productService;

    // GET /api/products
    @GET
    public Response getAllProducts() {
        try {
            List<Products> products = productService.getAllProducts();
            List<ProductDTO> result = products.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch products\"}").build();
        }
    }

    // GET /api/products/{id}
    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") int id) {
        try {
            Products p = productService.getProductById(id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Product not found\"}").build();
            }
            return Response.ok(toDTO(p)).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch product\"}").build();
        }
    }

    // GET /api/products/lowstock
    @GET
    @Path("/lowstock")
    public Response getLowStockProducts() {
        try {
            List<Products> products = productService.getLowStockProducts();
            List<ProductDTO> result = products.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch low stock products\"}").build();
        }
    }

    // GET /api/products/search?category=Electronics
    @GET
    @Path("/search")
    public Response getProductsByCategory(@QueryParam("category") String categoryName) {
        try {
            List<ProductDTO> result = productService.getAllProducts().stream()
                    .filter(p -> p.getCategoryId() != null &&
                            p.getCategoryId().getCategoryName().equalsIgnoreCase(categoryName))
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to search products\"}").build();
        }
    }

    private ProductDTO toDTO(Products p) {
        ProductDTO dto = new ProductDTO();
        dto.productId    = p.getProductId();
        dto.name         = p.getName();
        dto.description  = p.getDescription();
        dto.price        = p.getPrice() != null ? p.getPrice().doubleValue() : 0;
        dto.quantity     = p.getQuantity();
        dto.reorderLevel = p.getReorderLevel();
        dto.category     = p.getCategoryId() != null ? p.getCategoryId().getCategoryName() : null;
        dto.lowStock     = p.getQuantity() <= p.getReorderLevel();
        return dto;
    }

    public static class ProductDTO {
        public int productId;
        public String name;
        public String description;
        public double price;
        public int quantity;
        public int reorderLevel;
        public String category;
        public boolean lowStock;
    }
}
