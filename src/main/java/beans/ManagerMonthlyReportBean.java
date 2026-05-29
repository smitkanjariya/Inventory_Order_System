package beans;

import ejb.ManagerServiceLocal;
import ejb.PaymentServiceLocal;
import entity.Payments;
import entity.Request;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import session.UserSession;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Named("managerMonthlyReportBean")
@ViewScoped
public class ManagerMonthlyReportBean implements Serializable {

    @EJB private PaymentServiceLocal  paymentService;
    @EJB private ManagerServiceLocal  managerService;
    @Inject private UserSession       userSession;
    @Inject private ManagerBean       managerBean;

    private int selectedYear;
    private List<Integer> availableYears;
    private List<MonthRow> monthRows;

    // ── summary totals ────────────────────────────────────
    private BigDecimal yearTotalRevenue  = BigDecimal.ZERO;
    private long       yearTotalProducts = 0;
    private long       yearTotalRequests = 0;

    public static class MonthRow implements Serializable {
        private final String     monthName;
        private final int        monthNum;
        private final long       productsSold;
        private final long       requestCount;
        private final BigDecimal revenue;
        private final long       paymentCount;

        public MonthRow(String monthName, int monthNum, long productsSold,
                        long requestCount, BigDecimal revenue, long paymentCount) {
            this.monthName    = monthName;
            this.monthNum     = monthNum;
            this.productsSold = productsSold;
            this.requestCount = requestCount;
            this.revenue      = revenue;
            this.paymentCount = paymentCount;
        }

        public String     getMonthName()    { return monthName; }
        public int        getMonthNum()     { return monthNum; }
        public long       getProductsSold() { return productsSold; }
        public long       getRequestCount() { return requestCount; }
        public BigDecimal getRevenue()      { return revenue; }
        public long       getPaymentCount() { return paymentCount; }
        public boolean    isHasData()       { return revenue.compareTo(BigDecimal.ZERO) > 0 || requestCount > 0; }
        
        public int getBarHeight(BigDecimal yearTotal) {
            if (yearTotal.compareTo(BigDecimal.ZERO) <= 0 || revenue.compareTo(BigDecimal.ZERO) <= 0) {
                return 4;
            }
            return revenue.multiply(BigDecimal.valueOf(140))
                    .divide(yearTotal, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }
    }

    @PostConstruct
    public void init() {
        selectedYear = Calendar.getInstance().get(Calendar.YEAR);
        buildAvailableYears();
        loadReport();
    }

    private void buildAvailableYears() {
        int current = Calendar.getInstance().get(Calendar.YEAR);
        availableYears = new ArrayList<>();
        for (int y = current; y >= current - 4; y--) availableYears.add(y);
    }

    public void loadReport() {
        int managerId = managerBean.getManager().getUserId();

        List<Payments> payments = paymentService.getPaymentsByManager(managerId)
                .stream()
                .filter(p -> p.getStatus() == Payments.Status.COMPLETED
                          && p.getPaymentDate() != null)
                .collect(Collectors.toList());

        List<Request> paidRequests = managerService.getRequestsByManager(managerId)
                .stream()
                .filter(r -> r.getStatus() == Request.Status.PAID
                          && r.getCreatedAt() != null)
                .collect(Collectors.toList());

        monthRows = new ArrayList<>();
        yearTotalRevenue  = BigDecimal.ZERO;
        yearTotalProducts = 0;
        yearTotalRequests = 0;

        // First pass: calculate totals
        for (int m = 1; m <= 12; m++) {
            final int month = m;
            BigDecimal revenue = payments.stream()
                    .filter(p -> {
                        Calendar c = Calendar.getInstance();
                        c.setTime(p.getPaymentDate());
                        return c.get(Calendar.YEAR) == selectedYear
                            && c.get(Calendar.MONTH) + 1 == month;
                    })
                    .map(Payments::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            yearTotalRevenue = yearTotalRevenue.add(revenue);
        }

        // Second pass: build rows with bar heights
        for (int m = 1; m <= 12; m++) {
            final int month = m;

            List<Payments> monthPayments = payments.stream()
                    .filter(p -> {
                        Calendar c = Calendar.getInstance();
                        c.setTime(p.getPaymentDate());
                        return c.get(Calendar.YEAR) == selectedYear
                            && c.get(Calendar.MONTH) + 1 == month;
                    }).collect(Collectors.toList());

            BigDecimal revenue = monthPayments.stream()
                    .map(Payments::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Request> monthRequests = paidRequests.stream()
                    .filter(r -> {
                        Calendar c = Calendar.getInstance();
                        c.setTime(r.getCreatedAt());
                        return c.get(Calendar.YEAR) == selectedYear
                            && c.get(Calendar.MONTH) + 1 == month;
                    }).collect(Collectors.toList());

            long productsSold = monthRequests.stream()
                    .mapToLong(Request::getQuantity).sum();



            String monthName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthRows.add(new MonthRow(monthName, m, productsSold,
                    monthRequests.size(), revenue, monthPayments.size()));

            yearTotalProducts += productsSold;
            yearTotalRequests += monthRequests.size();
        }
    }

    // ── best month ────────────────────────────────────────
    public MonthRow getBestMonth() {
        if (monthRows == null) return null;
        return monthRows.stream()
                .max(Comparator.comparing(MonthRow::getRevenue))
                .filter(MonthRow::isHasData)
                .orElse(null);
    }

    public int        getSelectedYear()     { return selectedYear; }
    public void       setSelectedYear(int v){ this.selectedYear = v; }
    public List<Integer> getAvailableYears(){ return availableYears; }
    public List<MonthRow> getMonthRows()    { return monthRows; }
    public BigDecimal getYearTotalRevenue() { return yearTotalRevenue; }
    public long       getYearTotalProducts(){ return yearTotalProducts; }
    public long       getYearTotalRequests(){ return yearTotalRequests; }
}
