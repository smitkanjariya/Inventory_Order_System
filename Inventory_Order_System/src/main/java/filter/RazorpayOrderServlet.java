package filter;

import com.razorpay.RazorpayClient;
import ejb.PaymentServiceLocal;
import entity.Payments;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/razorpay-create-order")
public class RazorpayOrderServlet extends HttpServlet {

    private static final String RZP_KEY_ID     = "rzp_test_SXr0vlkLDQmIKb";
    private static final String RZP_KEY_SECRET = "3Y3bau3Byzq7eocD2i8UQGwH";

    @EJB private PaymentServiceLocal paymentService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            int managerId  = Integer.parseInt(req.getParameter("managerId"));
            int customerId = Integer.parseInt(req.getParameter("customerId"));
            long amountPaise = Long.parseLong(req.getParameter("amount")); // already in paise

            // Check not already paid
            List<Payments> existing = paymentService.getPaymentsByCustomerAndManager(customerId, managerId);
            boolean alreadyPaid = existing.stream().anyMatch(p -> p.getStatus() == Payments.Status.COMPLETED
                    && p.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue() == amountPaise);
            if (alreadyPaid) {
                resp.getWriter().write("{\"error\":\"Already paid\"}");
                return;
            }

            RazorpayClient client = new RazorpayClient(RZP_KEY_ID, RZP_KEY_SECRET);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt",  "rcpt_mgr_" + managerId + "_cust_" + customerId);

            com.razorpay.Order order = client.orders.create(orderRequest);

            JSONObject result = new JSONObject();
            result.put("orderId", order.get("id").toString());
            result.put("amount",  String.valueOf(amountPaise));
            resp.getWriter().write(result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
