package com.manager.auth_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. OMITIR FILTRO PARA ENDPOINTS PÚBLICOS DEL AUTH-SERVICE
        String path = request.getServletPath();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/public/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            try {
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);
                    
                    if (username != null && role != null) {
                        // Importante: Spring Security espera "ROLE_ADMIN", no solo "ADMIN"
                        String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(formattedRole);
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                        auth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));                            

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        logger.info("✅ Usuario autenticado en Auth-Service: {} con rol {}", username, formattedRole);
                    }
                }
            } catch (Exception e) {
                logger.error("❌ Error validando token JWT en Auth-Service: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);

    }

}