package com.myhome.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("My Homepage Backend API")
						.version("v1")
						.description("Personal technology platform backend API."))
				.servers(List.of(new Server()
						.url("/")
						.description("Current server")));
	}
}
