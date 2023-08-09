package com.ie.pdf2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@RestController
@Order(1)
@WebFilter(
        filterName = "MainFilter",
        urlPatterns = {"/*"})
public class MainFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // 跨域配置 这里是最好的
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PATCH, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");


        // 拿到ip地址
        String ip = ((HttpServletRequest) servletRequest).getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = ((HttpServletRequest) servletRequest).getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = ((HttpServletRequest) servletRequest).getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = servletRequest.getRemoteAddr();
        }

        HttpServletRequest servletPath = (HttpServletRequest) servletRequest;
        String path = servletPath.getRequestURI();


        String headerStr = servletPath.getHeader("Referer");
        String host = servletPath.getHeader("Host");

        log.info("===================================");
        log.info("host=" + host);
        log.info(headerStr);
        log.info(ip);
        log.info(path);

        filterChain.doFilter(servletRequest, response);

    }

    @Override
    public void destroy() {
    }
}
