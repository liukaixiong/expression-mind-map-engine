package com.liukx.expression.engine.server.config;

import cn.hutool.extra.spring.SpringUtil;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.handler.LoginHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.*;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
//        registry.addResourceHandler("/lib/**").addResourceLocations("classpath:/static/lib/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/template/**").addResourceLocations("classpath:/template/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginHandler).addPathPatterns("/**")
                .excludePathPatterns(BaseConstants.HTML_LOGIN_PATH, BaseConstants.LOGIN_URL)
                // 待优化，还是需要鉴权等等
                .excludePathPatterns("/server/**")
                .excludePathPatterns("/js/**")
        ;
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
