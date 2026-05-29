package filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@WebServlet("/product-images/*")
public class ProductImageServlet extends HttpServlet {

    // Payara docroot — permanent, never wiped on redeploy
    public static final String UPLOAD_DIR =
        "D:\\SETUPDOWNLOADED\\payara-6.2025.11\\payara6\\glassfish\\domains\\domain1\\docroot\\product-images";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo(); // e.g. /uuid.jpg
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Strip leading slash
        String fileName = pathInfo.substring(1);
        // If stored value still has the 'product-images/' prefix, strip it
        if (fileName.startsWith("product-images/")) {
            fileName = fileName.substring("product-images/".length());
        }
        // Security: reject any path traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File file = new File(UPLOAD_DIR, fileName);
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) mimeType = "application/octet-stream";

        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());
        resp.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file.toPath(), resp.getOutputStream());
    }
}
