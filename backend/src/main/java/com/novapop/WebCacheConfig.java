package com.novapop;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Caché del navegador para los archivos estáticos (lo detectó la auditoría de Lighthouse).
 * Las imágenes (logo, íconos, avatares) casi no cambian: se guardan 7 días. Las páginas, el CSS y el JS usan
 * "no-cache" (application.properties): el navegador las guarda pero pregunta si cambiaron, así cada despliegue
 * se ve al instante. La API sigue sin caché (SecurityConfig).
 */
@Configuration
public class WebCacheConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}
