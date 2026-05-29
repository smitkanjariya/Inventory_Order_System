package ejb;

import entity.Categories;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class CategoryService implements CategoryServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    public void addCategory(Categories cat) {
        em.persist(cat);
    }

    public void updateCategory(Categories cat) {
        em.merge(cat);
    }

    public void deleteCategory(int id) {
        Categories cat = em.find(Categories.class, id);
        if (cat != null) {
            em.remove(cat);
        }
    }

    public Categories getCategoryById(int id) {
        return em.find(Categories.class, id);
    }

    public List<Categories> getAllCategories() {
        return em.createNamedQuery("Categories.findAll", Categories.class).getResultList();
    }

    public List<Categories> getCategoriesByManager(int userId) {
        return em.createNamedQuery("Categories.findByManager", Categories.class)
                .setParameter("managerId", userId)
                .getResultList();
    }

    public List<Categories> getCategoriesByManagerIncludingShared(int userId) {
        return em.createQuery(
                "SELECT c FROM Categories c WHERE c.managerId.userId = :userId OR c.managerId IS NULL",
                Categories.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
