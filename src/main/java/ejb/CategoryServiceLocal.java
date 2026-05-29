package ejb;

import entity.Categories;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CategoryServiceLocal {
    void addCategory(Categories cat);
    void updateCategory(Categories cat);
    void deleteCategory(int id);
    Categories getCategoryById(int id);
    List<Categories> getAllCategories();
    List<Categories> getCategoriesByManager(int managerId);
    List<Categories> getCategoriesByManagerIncludingShared(int managerId);
}
