//package com.sparta.settlementprogram.config;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//@Configuration
//public class GatewayConfig {
//
//    @Bean
//    @Profile("local")
//    public RouteLocator localRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("local_route", r -> r.path("/api/**")
//                        .uri("http://localhost:8080"))  // 로컬 환경의 마이크로서비스 주소
//                .build();
//    }
//
//
//    @Bean
//    @Profile("docker")
//    public RouteLocator dockerRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("docker_route", r -> r.path("/api/**")
//                        .uri("http://monolith-service:8080"))  // Docker 네트워크 내 마이크로서비스 주소
//                .build();
//    }
//}
