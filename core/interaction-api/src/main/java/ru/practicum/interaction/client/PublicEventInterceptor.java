package ru.practicum.interaction.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class PublicEventInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String requestURI = request.getRequestURI();
            String remoteAddr = request.getRemoteAddr();

            if (requestURI != null) {
                template.header("X-Request-URI", requestURI);
            }
            if (remoteAddr != null) {
                template.header("X-Remote-Addr", remoteAddr);
            }
        }
    }
}
