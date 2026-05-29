package ejb;

import entity.Roles;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class UserService implements UserServiceLocal {

    @PersistenceContext(unitName = "inventoryPU")
    private EntityManager em;

    public void registerUser(Users user) {
        em.persist(user);
    }

    public List<Users> getAllUsers() {
        return em.createNamedQuery("Users.findAll", Users.class).getResultList();
    }

    public Users getUserById(int id) {
        return em.find(Users.class, id);
    }

    public void updateUser(Users user) {
        em.merge(user);
    }

    public void deleteUser(int id) {
        Users user = em.find(Users.class, id);
        if (user != null) {
            em.remove(user);
        }
    }

    public Users getUserByEmail(String email) {
        try {
            return em.createNamedQuery("Users.findByEmail", Users.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Users> getUsersByRole(String roleName) {
        return em.createNamedQuery("Users.findByRole", Users.class)
                .setParameter("roleName", roleName)
                .getResultList();
    }

    public Roles getCustomerRole() {
        return em.find(Roles.class, 4);
    }

    public long getUserCount() {
        return (long) em.createQuery("SELECT COUNT(u) FROM Users u").getSingleResult();
    }

    public List<Roles> getAllRoles() {
        return em.createQuery("SELECT r FROM Roles r", Roles.class).getResultList();
    }
}
