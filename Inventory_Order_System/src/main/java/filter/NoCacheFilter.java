package filter;

import java.io.IOException;
import jakarta.faces.application.ResourceHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(filterName = "NoCacheFilter", urlPatterns = {"/*"})
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri        = req.getRequestURI();
        String contextPath = req.getContextPath();

        boolean isJsfResource = uri.startsWith(contextPath + ResourceHandler.RESOURCE_IDENTIFIER);
        boolean isLoginPage   = uri.endsWith("login.xhtml");

        // Apply no-cache only to protected pages, not to JSF resources or the login page
        if (!isJsfResource && !isLoginPage) {
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);
        }

        chain.doFilter(request, response);
    }

    @Override public void init(FilterConfig filterConfig) throws ServletException {}
    @Override public void destroy() {}
}
