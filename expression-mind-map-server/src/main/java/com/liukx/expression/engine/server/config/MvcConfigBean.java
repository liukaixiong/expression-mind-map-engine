package com.liukx.expression.engine.server.config;

import cn.hutool.extra.spring.SpringUtil;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.handler.LoginHandler;
import com.liukx.expression.engine.server.handler.StaticResourceCacheInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.VersionResourceResolver;

/**
 * mvc层定义
 *
 * @author Liuhx
 * @create 2018/6/14 18:41
 * @email liuhx@elab-plus.com
 **/
@Configuration
@EnableConfigurationProperties(value = {ExpressionServerProperties.class})
@EnableWebMvc
@ComponentScan(basePackages = {"com.liukx.expression.engine.server.controller"})
@Import({SpringUtil.class})
public class MvcConfigBean implements WebMvcConfigurer {

    @Autowired
    private LoginHandler loginHandler;

    @Autowired
    private StaticResourceCacheInterceptor staticResourceCacheInterceptor;

    /**
     * 静态资源版本号（使用构建时间戳，每次打包自动更新）
     * 可以在 application.yml 中覆盖：static-resource.version
     */
    private String staticResourceVersion = String.valueOf(System.currentTimeMillis() / 1000);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用版本号策略：当文件内容变化时，MD5哈希会变化，浏览器会自动下载新文件
        int cachePeriod = 60 * 60 * 24 * 30; // 30天缓存

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(cachePeriod)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver()
                        .addContentVersionStrategy("/js/**"));

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(cachePeriod)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver()
                        .addContentVersionStrategy("/css/**"));

        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/")
                .setCachePeriod(cachePeriod);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(cachePeriod);

        registry.addResourceHandler("/template/**")
                .addResourceLocations("classpath:/template/");

        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 暴露 ResourceUrlProvider 给模板使用
     */
    @Bean
    public ResourceUrlProvider resourceUrlProvider() {
        return new ResourceUrlProvider();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(loginHandler).addPathPatterns("/**")
                .excludePathPatterns(BaseConstants.HTML_LOGIN_PATH, BaseConstants.LOGIN_URL)
                // 待优化，还是需要鉴权等等
                .excludePathPatterns("/server/**")
                // 排除静态资源路径，避免登录页面的背景图等资源被拦截
                .excludePathPatterns("/js/**", "/css/**", "/fonts/**", "/images/**", "/webjars/**")
        ;

        // 静态资源缓存控制拦截器
        registry.addInterceptor(staticResourceCacheInterceptor)
                .addPathPatterns("/js/**", "/css/**", "/fonts/**", "/images/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 跨域问题
        registry.addMapping("/**") // 允许跨域的路径
                .allowedOrigins("*") // 允许跨域请求的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的请求方法
                .allowedHeaders("*") // 允许的请求头
//                .allowCredentials(true) // 是否允许证书（cookies）
                .maxAge(3600);
    }

}
