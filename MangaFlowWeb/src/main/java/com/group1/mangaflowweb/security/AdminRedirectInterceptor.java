package com.group1.mangaflowweb.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminRedirectInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            // Check if user is ADMIN
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
            
            String requestURI = request.getRequestURI();
            
            // If ADMIN is trying to access non-admin endpoints (except static files), redirect to /admin
            if (isAdmin && !requestURI.startsWith("/admin") 
                    && !requestURI.startsWith("/css")
                    && !requestURI.startsWith("/js")
                    && !requestURI.startsWith("/images")
                    && !requestURI.startsWith("/fonts")
                    && !requestURI.startsWith("/bootstrap")
                    && !requestURI.startsWith("/error")
                    && !requestURI.startsWith("/logout")
                    && !requestURI.startsWith("/api/auth")) {
                
                response.sendRedirect("/admin");
                return false;
            }
        }
        
        return true;
    }
}

