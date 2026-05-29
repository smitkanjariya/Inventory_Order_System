package ejb;

import entity.Orders;
import entity.Request;
import entity.Users;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ManagerServiceLocal {
    Users findByEmail(String email);
    void updateOrganizationName(int userId, String orgName);
    long getProductCountByManager(int userId);
    long getPendingRequestCount(int userId);
    List<Request> getRequestsByManager(int userId);
    List<Request> getRequestsByCustomer(int customerId);
    void updateRequestStatus(int requestId, Request.Status status);
    void saveRequest(Request request);
    List<Request> getAcceptedRequestsByCustomer(int customerId);
    Orders createOrderFromRequest(Request r);
}
