package site.klade.webapp.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * DEV ONLY: Allows libGDX iframe to load from Vaadin app
 * WARNING: Delete this file before production - it disables CORS protection
 */
@Configuration
@SuppressWarnings("unused")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/stage/**")
                .allowedOrigins("http://localhost:8082")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowCredentials(true);
        
        // Also allow Vaadin to access libGDX assets
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}