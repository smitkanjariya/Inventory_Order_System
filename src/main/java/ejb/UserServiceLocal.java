package ejb;

import entity.Roles;
import entity.Users;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface UserServiceLocal {
    void registerUser(Users user);
    List<Users> getAllUsers();
    Users getUserById(int id);
    void updateUser(Users user);
    void deleteUser(int id);
    Users getUserByEmail(String email);
    List<Users> getUsersByRole(String roleName);
    Roles getCustomerRole();
    long getUserCount();
    List<Roles> getAllRoles();
}
