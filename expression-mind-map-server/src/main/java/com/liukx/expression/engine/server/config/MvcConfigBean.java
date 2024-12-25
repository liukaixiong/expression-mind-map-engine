package com.liukx.expression.engine.server.config;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc层定义
 *
 * @author Liuhx
 * @create 2018/6/14 18:41
 * @email liuhx@elab-plus.com
 **/
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.liukx.expression.engine.server.controller"})
@Import({SpringUtil.class})
public class MvcConfigBean implements WebMvcConfigurer {

//    @Autowired
//    private LoginHandler loginHandler;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/lib/**").addResourceLocations("classpath:/static/lib/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/template/**").addResourceLocations("classpath:/template/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(loginHandler)
//                .addPathPatterns("/**")
//            //.excludePathPatterns(ServerConstants.LOGIN_HTML_PATH, ServerConstants.LOGIN_PATH)
//            .excludePathPatterns("/js/**", "/css/**", "/lib/**", "/fonts/**", "/images/**");
    }

}
