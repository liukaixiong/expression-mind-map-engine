package com.liukx.expression.engine.server.handler;

import com.liukx.expression.engine.jdk.handler.AbstractLoginHandler;
import org.springframework.stereotype.Component;

/**
 * 静态资源缓存控制拦截器
 * 为静态资源设置合适的缓存策略，解决浏览器缓存问题
 *
 * @author expression-engine
 */
@Component
public class StaticResourceCacheInterceptor extends AbstractLoginHandler {

    /**
     * 静态资源版本号（构建时自动生成）
     */
    private final String version = System.currentTimeMillis() + "";


    @Override
    public boolean preHandle0(Object request, Object response, Object handler) throws Exception {
        String uri = getRequestURI(request);

        // 只处理静态资源
        if (isStaticResource(uri)) {
            // 设置 ETag 为版本号，当版本号变化时，浏览器会认为资源已更改
            addHeader(response, "ETag", "\"" + version + "\"");

            // 设置 Cache-Control
            // no-cache: 每次使用前都需要验证（通过 ETag）
            // must-revalidate: 过期后必须重新验证
            // max-age: 缓存时间（秒）
            addHeader(response, "Cache-Control", "no-cache, must-revalidate");

            // 设置 Last-Modified 为当前时间
            addHeader(response, "Last-Modified", String.valueOf(System.currentTimeMillis()));
        }

        return true;
    }

    /**
     * 判断是否是静态资源
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/js/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/fonts/") ||
                uri.startsWith("/images/");
    }
}
