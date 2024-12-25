//package com.liukx.expression.engine.server.config;
//
//import io.swagger.annotations.ApiOperation;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.oas.annotations.EnableOpenApi;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author v_tanghanlin@saicmotor.com
// */
//@EnableOpenApi
//@Configuration
//public class Swagger3Config implements WebMvcConfigurer {
//
//    @Bean
//    public Docket createRestApi() {
//        // List<Response> responseMessageList = new ArrayList<>();
//        // Arrays.stream(ErrorEnum.values()).forEach(errorEnums -> {
//        // responseMessageList.add(new
//        // ResponseBuilder().code(String.valueOf(errorEnums.code()))
//        // .description(errorEnums.message()).build());
//        // });
//        return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo())
//            // .globalResponses(HttpMethod.GET,
//            // responseMessageList).globalResponses(HttpMethod.POST, responseMessageList)
//            // .globalResponses(HttpMethod.PUT, responseMessageList)
//            // .globalResponses(HttpMethod.DELETE, responseMessageList)
//            .select()
//                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
//                .paths(PathSelectors.any())
//            .build()
//            // .globalRequestParameters(getGlobalRequestParameters())
//            .securitySchemes(securitySchemes()).securityContexts(securityContexts());
//
//    }
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//    // // 生成全局通用参数
//    // private List<RequestParameter> getGlobalRequestParameters() {
//    // List<RequestParameter> parameters = new ArrayList<>();
//    // parameters.add(new
//    // RequestParameterBuilder().name("userId").description("userId").required(false)
//    // .in(ParameterType.HEADER).query(q -> q.model(m ->
//    // m.scalarModel(ScalarType.STRING))).build());
//    // parameters.add(new
//    // RequestParameterBuilder().name("telphone").description("telphone").required(false)
//    // .in(ParameterType.HEADER).query(q -> q.model(m ->
//    // m.scalarModel(ScalarType.STRING))).build());
//    // parameters
//    // .add(new
//    // RequestParameterBuilder().name("watch-man-token").description("watch-man-token").required(false)
//    // .in(ParameterType.HEADER).query(q -> q.model(m ->
//    // m.scalarModel(ScalarType.STRING))).build());
//    // return parameters;
//    // }
//
//    private List<SecurityScheme> securitySchemes() {
//        List<SecurityScheme> list = new ArrayList<>();
////        list.add(new ApiKey(TokenConstants.HEADER_TOKEN, TokenConstants.HEADER_TOKEN, ParameterType.HEADER.getIn()));
////        list.add(
////            new ApiKey(TokenConstants.HEADER_ACCOUNT, TokenConstants.HEADER_ACCOUNT, ParameterType.HEADER.getIn()));
////        list.add(new ApiKey(TokenConstants.HEADER_USERID, TokenConstants.HEADER_USERID, ParameterType.HEADER.getIn()));
////        list.add(
////            new ApiKey(TokenConstants.HEADER_TELPHONE, TokenConstants.HEADER_TELPHONE, ParameterType.HEADER.getIn()));
////        list.add(new ApiKey(TokenConstants.HEADER_WATCH_MAN_TOKEN, TokenConstants.HEADER_WATCH_MAN_TOKEN,
////            ParameterType.HEADER.getIn()));
//
//        return list;
//    }
//
//    private List<SecurityContext> securityContexts() {
//        List<SecurityContext> securityContexts = new ArrayList<>();
//        securityContexts.add(SecurityContext.builder().securityReferences(defaultAuth())
//            /* .forPaths(PathSelectors.regex("^(?!auth).*$")) */.build());
//        return securityContexts;
//    }
//
//    List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        List<SecurityReference> securityReferences = new ArrayList<>();
////        securityReferences.add(new SecurityReference(TokenConstants.HEADER_TOKEN, authorizationScopes));
////        securityReferences.add(new SecurityReference(TokenConstants.HEADER_ACCOUNT, authorizationScopes));
////        securityReferences.add(new SecurityReference(TokenConstants.HEADER_USERID, authorizationScopes));
////        securityReferences.add(new SecurityReference(TokenConstants.HEADER_TELPHONE, authorizationScopes));
////        securityReferences.add(new SecurityReference(TokenConstants.HEADER_WATCH_MAN_TOKEN, authorizationScopes));
//        return securityReferences;
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder().title("engine-server").description("奖励引擎，规则引擎 一体的裂变活动中台")
//            .contact(new Contact("SAIC", "#", "v_tanghanlin@saicmotor.com")).version("1.0").build();
//    }
//}
