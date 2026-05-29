package filter;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import ejb.ManagerServiceLocal;
import ejb.PaymentServiceLocal;
import entity.Payments;
import entity.Request;
import entity.Users;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/download-report")
public class PdfReportServlet extends HttpServlet {

    @EJB private ManagerServiceLocal managerService;
    @EJB private PaymentServiceLocal paymentService;

    private static final DeviceRgb TEAL       = new DeviceRgb(13, 148, 136);
    private static final DeviceRgb TEAL_LIGHT = new DeviceRgb(240, 253, 250);
    private static final DeviceRgb HEADER_BG  = new DeviceRgb(15, 23, 42);
    private static final DeviceRgb GREY       = new DeviceRgb(100, 116, 139);
    private static final SimpleDateFormat SDF  = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ── read params ──────────────────────────────────
        String type       = req.getParameter("type");       // "pre" or "post"
        String managerStr = req.getParameter("managerId");
        String custStr    = req.getParameter("customerId");

        if (type == null || managerStr == null || custStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        // ── auth: validate customer exists ──────────────
        int managerId  = Integer.parseInt(managerStr);
        int customerId = Integer.parseInt(custStr);

        String requestIdStr = req.getParameter("requestId");

        List<Request> requests = managerService.getRequestsByCustomer(customerId)
                .stream()
                .filter(r -> r.getManagerId() != null && r.getManagerId().getUserId() == managerId)
                .filter(r -> "pre".equals(type)
                        ? r.getStatus() == Request.Status.ACCEPTED
                        : r.getStatus() == Request.Status.PAID)
                .filter(r -> requestIdStr == null || r.getRequestId().equals(Integer.parseInt(requestIdStr)))
                .collect(Collectors.toList());

        if (requests.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No accepted requests found");
            return;
        }

        Users manager  = requests.get(0).getManagerId();
        Users customer = requests.get(0).getCustomerId();

        BigDecimal total = requests.stream()
                .map(r -> r.getProductId().getPrice().multiply(BigDecimal.valueOf(r.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payments> receipts = paymentService.getPaymentsByCustomerAndManager(customerId, managerId)
                .stream()
                .filter(p -> p.getStatus() == Payments.Status.COMPLETED)
                .filter(p -> requestIdStr == null || p.getRequestId() == null
                        || p.getRequestId().equals(Integer.parseInt(requestIdStr)))
                .collect(Collectors.toList());
        // fallback: if no request-specific payment found, show all completed payments for this manager
        if (receipts.isEmpty() && "post".equals(type)) {
            receipts = paymentService.getPaymentsByCustomerAndManager(customerId, managerId)
                    .stream()
                    .filter(p -> p.getStatus() == Payments.Status.COMPLETED)
                    .collect(Collectors.toList());
        }

        // ── set response headers ─────────────────────────
        String fileName = ("pre".equals(type) ? "Pre_Payment_Report" : "Payment_Receipt")
                + "_" + manager.getOrganizationName().replaceAll("\\s+", "_")
                + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // ── generate PDF ─────────────────────────────────
        PdfWriter  writer  = new PdfWriter(resp.getOutputStream());
        PdfDocument pdf    = new PdfDocument(writer);
        Document   doc     = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 40, 40, 40);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // ── header bar ───────────────────────────────────
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100));
        Cell headerCell = new Cell()
                .setBackgroundColor(HEADER_BG)
                .setPadding(16)
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph("pre".equals(type) ? "PRE-PAYMENT ORDER REPORT" : "PAYMENT RECEIPT")
                        .setFont(bold).setFontSize(18).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Inventory Order System")
                        .setFont(regular).setFontSize(10).setFontColor(new DeviceRgb(148, 163, 184))
                        .setTextAlignment(TextAlignment.CENTER));
        headerTable.addCell(headerCell);
        doc.add(headerTable);
        doc.add(new Paragraph("\n"));

        // ── info section ─────────────────────────────────
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(16);

        infoTable.addCell(infoCell("Supplier", manager.getOrganizationName(), bold, regular));
        infoTable.addCell(infoCell("Customer", customer.getName(), bold, regular));
        infoTable.addCell(infoCell("Supplier Email", manager.getEmail(), bold, regular));
        infoTable.addCell(infoCell("Customer Email", customer.getEmail(), bold, regular));
        infoTable.addCell(infoCell("Report Type",
                "pre".equals(type) ? "Pre-Payment (Pending)" : "Post-Payment (Completed)", bold, regular));
        infoTable.addCell(infoCell("Generated On", SDF.format(new Date()), bold, regular));
        doc.add(infoTable);

        // ── products table ───────────────────────────────
        doc.add(new Paragraph("Order Details")
                .setFont(bold).setFontSize(13).setFontColor(TEAL).setMarginBottom(8));

        Table prodTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 1, 2, 2}))
                .setWidth(UnitValue.createPercentValue(100));

        // table header
        String[] headers = {"#", "Product", "Category", "Qty", "Unit Price", "Subtotal"};
        for (String h : headers) {
            prodTable.addHeaderCell(new Cell()
                    .setBackgroundColor(TEAL)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8)
                    .add(new Paragraph(h).setFont(bold).setFontSize(10)
                            .setFontColor(ColorConstants.WHITE)));
        }

        // table rows
        boolean alt = false;
        for (Request r : requests) {
            DeviceRgb rowBg = alt ? TEAL_LIGHT : new DeviceRgb(255, 255, 255);
            BigDecimal subtotal = r.getProductId().getPrice().multiply(BigDecimal.valueOf(r.getQuantity()));
            String[] vals = {
                "#" + r.getRequestId(),
                r.getProductId().getName(),
                r.getProductId().getCategoryId() != null ? r.getProductId().getCategoryId().getCategoryName() : "—",
                String.valueOf(r.getQuantity()),
                "Rs." + r.getProductId().getPrice().toPlainString(),
                "Rs." + subtotal.toPlainString()
            };
            for (String v : vals) {
                prodTable.addCell(new Cell()
                        .setBackgroundColor(rowBg)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(new DeviceRgb(241, 245, 249), 1))
                        .setPadding(8)
                        .add(new Paragraph(v).setFont(regular).setFontSize(10)));
            }
            alt = !alt;
        }
        doc.add(prodTable);

        // ── total row ────────────────────────────────────
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{4, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(4);
        totalTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Grand Total").setFont(bold).setFontSize(13)
                        .setTextAlignment(TextAlignment.RIGHT)));
        totalTable.addCell(new Cell()
                .setBackgroundColor(TEAL).setBorder(Border.NO_BORDER).setPadding(8)
                .add(new Paragraph("Rs." + total.toPlainString())
                        .setFont(bold).setFontSize(13).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)));
        doc.add(totalTable);

        // ── payment status section ──────────────────────
        doc.add(new Paragraph("\n"));
        
        if ("pre".equals(type)) {
            // Pre-payment: Show pending payment notice
            Table statusTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(8);
            Cell statusCell = new Cell()
                    .setBackgroundColor(new DeviceRgb(254, 243, 199))  // Yellow background
                    .setBorder(new SolidBorder(new DeviceRgb(251, 191, 36), 2))
                    .setPadding(16)
                    .add(new Paragraph("⚠ PAYMENT PENDING")
                            .setFont(bold).setFontSize(14)
                            .setFontColor(new DeviceRgb(146, 64, 14))
                            .setTextAlignment(TextAlignment.CENTER))
                    .add(new Paragraph("This is a pre-payment order report. Payment has not been completed yet.")
                            .setFont(regular).setFontSize(10)
                            .setFontColor(new DeviceRgb(120, 53, 15))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(4));
            statusTable.addCell(statusCell);
            doc.add(statusTable);
            
            // Add payment instructions
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Payment Instructions:")
                    .setFont(bold).setFontSize(11).setFontColor(TEAL).setMarginBottom(6));
            doc.add(new Paragraph("• Please complete the payment to confirm this order")
                    .setFont(regular).setFontSize(10).setMarginLeft(10));
            doc.add(new Paragraph("• Total amount payable: Rs." + total.toPlainString())
                    .setFont(regular).setFontSize(10).setMarginLeft(10));
            doc.add(new Paragraph("• After payment, download the payment receipt for your records")
                    .setFont(regular).setFontSize(10).setMarginLeft(10));
            
        } else if ("post".equals(type) && !receipts.isEmpty()) {
            // Post-payment: Show payment receipt details
            Table statusTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(8);
            Cell statusCell = new Cell()
                    .setBackgroundColor(new DeviceRgb(209, 250, 229))  // Green background
                    .setBorder(new SolidBorder(new DeviceRgb(16, 185, 129), 2))
                    .setPadding(16)
                    .add(new Paragraph("✓ PAYMENT COMPLETED")
                            .setFont(bold).setFontSize(14)
                            .setFontColor(new DeviceRgb(6, 95, 70))
                            .setTextAlignment(TextAlignment.CENTER))
                    .add(new Paragraph("Payment has been successfully processed and confirmed.")
                            .setFont(regular).setFontSize(10)
                            .setFontColor(new DeviceRgb(6, 78, 59))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(4));
            statusTable.addCell(statusCell);
            doc.add(statusTable);
            
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Payment Receipt Details")
                    .setFont(bold).setFontSize(13).setFontColor(TEAL).setMarginBottom(8));

            Table recTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            String[] rh = {"Receipt #", "Amount Paid", "Status", "Date & Time"};
            for (String h : rh) {
                recTable.addHeaderCell(new Cell()
                        .setBackgroundColor(new DeviceRgb(16, 185, 129))
                        .setBorder(Border.NO_BORDER).setPadding(8)
                        .add(new Paragraph(h).setFont(bold).setFontSize(10)
                                .setFontColor(ColorConstants.WHITE)));
            }
            for (Payments pay : receipts) {
                recTable.addCell(dataCell("#" + pay.getPaymentId(), regular));
                recTable.addCell(dataCell("Rs." + pay.getTotalAmount().toPlainString(), regular));
                recTable.addCell(dataCell(pay.getStatus().name(), regular));
                recTable.addCell(dataCell(SDF.format(pay.getPaymentDate()), regular));
            }
            doc.add(recTable);
        }

        // ── footer ───────────────────────────────────────
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("This is a system-generated report from Inventory Order System.")
                .setFont(regular).setFontSize(9).setFontColor(GREY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
    }

    private Cell infoCell(String label, String value, PdfFont bold, PdfFont regular) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(226, 232, 240), 1))
                .setPadding(8)
                .add(new Paragraph(label).setFont(bold).setFontSize(9)
                        .setFontColor(GREY))
                .add(new Paragraph(value != null ? value : "—").setFont(regular).setFontSize(11));
    }

    private Cell dataCell(String value, PdfFont regular) {
        return new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(241, 245, 249), 1))
                .setPadding(8)
                .add(new Paragraph(value != null ? value : "—").setFont(regular).setFontSize(10));
    }
}
