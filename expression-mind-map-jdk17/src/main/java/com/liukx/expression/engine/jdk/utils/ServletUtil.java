package com.liukx.expression.engine.jdk.utils;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServletUtil {

    /**
     * 根据名字获取cookie
     *
     * @param name cookie名字
     * @return Cookie
     */
    public static String getCookieByName(String name) {
        Map<String, String> cookieMap = readCookieMap(getCurrentRequest());
        return cookieMap.getOrDefault(name, null);
    }

    public static String getRequestURI() {
        return getCurrentRequest().getRequestURI();
    }

    /**
     * 获取当前请求的完整地址（URI + query string）。
     * 例如: /template/expression-rule-config.html?id=1
     */
    public static String getRequestUrlWithQuery() {
        HttpServletRequest request = getCurrentRequest();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString == null ? requestURI : requestURI + "?" + queryString;
    }

    /**
     * UTF-8 URL 编码
     */
    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 获取当前请求的指定 Header 值
     */
    public static String getHeader(String name) {
        return getCurrentRequest().getHeader(name);
    }

    /**
     * 获取当前请求的所有 Header 名（用于判断请求类型）
     */
    public static String getAcceptHeader() {
        return getCurrentRequest().getHeader("Accept");
    }

    public static boolean isXmlHttpRequest() {
        return "XMLHttpRequest".equals(getCurrentRequest().getHeader("X-Requested-With"));
    }

    /**
     * 将cookie封装到Map里面
     *
     * @param request
     * @return Map<String, Cookie>
     */
    private static Map<String, String> readCookieMap(HttpServletRequest request) {
        Map<String, String> cookieMap = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }
        return cookieMap;
    }

    public static HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes();
        return attributes.getResponse();
    }

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }

    /**
     * 保存Cookies
     *
     * @param name  cookie的名字
     * @param value cookie的值
     * @param time  cookie的存在时间(秒)
     */
    public static void setCookie(String name, String value, int time) {
        // new一个Cookie对象,键值对为参数
        Cookie cookie = new Cookie(name, value);
        // tomcat下多应用共享
        cookie.setPath("/");
        // 如果cookie的值中含有中文时，需要对cookie进行编码，不然会产生乱码
        try {
            URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // 单位：秒
        cookie.setMaxAge(time);
        // Secure 跟随请求协议：仅在 HTTPS 下启用，避免 HTTP 环境（本地/测试）下浏览器丢弃 cookie 导致无法登录
        cookie.setSecure("https".equalsIgnoreCase(getCurrentRequest().getScheme()));
        cookie.setHttpOnly(true);
        // 将Cookie添加到Response中,使之生效
        getCurrentResponse().addCookie(cookie); // addCookie后，如果已经存在相同名字的cookie，则最新的覆盖旧的cookie
    }

}
