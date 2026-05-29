package rest;

import ejb.CategoryServiceLocal;
import entity.Categories;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @EJB
    private CategoryServiceLocal categoryService;

    // GET /api/categories
    @GET
    public Response getAllCategories() {
        try {
            List<Categories> categories = categoryService.getAllCategories();
            List<CategoryDTO> result = categories.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch categories\"}").build();
        }
    }

    // GET /api/categories/{id}
    @GET
    @Path("/{id}")
    public Response getCategoryById(@PathParam("id") int id) {
        try {
            Categories c = categoryService.getCategoryById(id);
            if (c == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Category not found\"}").build();
            }
            return Response.ok(toDTO(c)).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("{\"error\":\"Failed to fetch category\"}").build();
        }
    }

    private CategoryDTO toDTO(Categories c) {
        CategoryDTO dto = new CategoryDTO();
        dto.categoryId   = c.getCategoryId();
        dto.categoryName = c.getCategoryName();
        return dto;
    }

    public static class CategoryDTO {
        public int categoryId;
        public String categoryName;
    }
}
