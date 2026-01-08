package com.example.lab4.webfilter;


import com.example.lab4.errors.PackNotFound;
import com.example.lab4.service.PackService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.Order;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

@WebFilter("/api/packs/*")
@Order(10)
public class PackWebFilter  implements Filter {

    private final PackService packService;

    public PackWebFilter(PackService packService) {
        this.packService = packService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;


        String method = req.getMethod();
        if (!("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method))) {
            chain.doFilter(req, res);
            return;
        }
        String resourceId = req.getRequestURI();
        if (resourceId.endsWith("/packs")) {
            chain.doFilter(req, res);
            return;
        }
        long lastModified;
        try {
            lastModified = getLastModifiedFor(resourceId);
        } catch (PackNotFound e) {
            return;
        }

        String etag = computeETag(resourceId, lastModified);
        ResponseWrapper responseWrapper = new ResponseWrapper(res);


        String ifNoneMatch = req.getHeader("If-None-Match");
        if (ifNoneMatch != null && matches(ifNoneMatch, etag)) {
            responseWrapper.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            responseWrapper.addHeader("ETag", etag);
            responseWrapper.addHeader("Cache-Control", "public, max-age=60");
            return;
        }


        chain.doFilter(req, responseWrapper);
        String body = responseWrapper.getBodyAsString();

        responseWrapper.addHeader("ETag", etag);
        responseWrapper.addHeader("Cache-Control", "public, max-age=60");
        response.getOutputStream().write(body.getBytes());
    }


    private long getLastModifiedFor(String resourceId) throws PackNotFound {
        int questionMarkIndex = !resourceId.contains("?") ? resourceId.length() : resourceId.indexOf("?");
        Long packId = Long.valueOf(resourceId.substring(resourceId.lastIndexOf('/') + 1, questionMarkIndex));
        return packService.getTimeStamp(packId);
    }

    private String computeETag(String resourceId, long lastModified) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((resourceId + ":" + lastModified).getBytes());
            String base64 = Base64.getEncoder().encodeToString(md.digest());
            return "\"sha256:" + base64 + "\"";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean matches(String ifNoneMatch, String currentETag) {
        if ("*".equals(ifNoneMatch.trim())) return true;
        for (String tag : ifNoneMatch.split(",")) {
            if (weakEquals(tag.trim(), currentETag)) return true;
        }
        return false;
    }

    private boolean weakEquals(String a, String b) {
        return stripWeak(a).equals(stripWeak(b));
    }

    private String stripWeak(String t) {
        t = t.trim();
        if (t.startsWith("W/")) t = t.substring(2);
        return t;
    }

}


class ResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                outputStream.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        super.flushBuffer();
        writer.flush();
    }

    public String getBodyAsString() {
        writer.flush();
        return outputStream.toString();
    }
}