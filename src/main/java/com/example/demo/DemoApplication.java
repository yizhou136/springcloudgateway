package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

//@Configuration
//@SpringBootApplication
public class DemoApplication {

	/*@Bean
	public RouteLocator customRouteLocator() {
		return Routes.locator()
				.route("test")
				.uri("http://httpbin.org:80")
				.predicate(host("**.abc.org").and(path("/image/png")))
				.addResponseHeader("X-TestHeader", "foobar")
				.and()
				.route("test2")
				.uri("http://httpbin.org:80")
				.predicate(path("/image/webp"))
				.add(addResponseHeader("X-AnotherHeader", "baz"))
				.and()
				.build();
	}*/
	public static void main2(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
