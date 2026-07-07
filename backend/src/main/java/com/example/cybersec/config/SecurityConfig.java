package com.example.cybersec.config;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
            .antMatchers("/api/auth/**", "/api/public/**", "/favicon.ico", "/api/resource/**", "/api/chat/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")
            .antMatchers("/api/student/**").hasRole("STUDENT")
            .anyRequest().authenticated()
        .and()
        .addFilterBefore(roleHeaderFilter(), UsernamePasswordAuthenticationFilter.class)
        .httpBasic();

    return http.build();
  }

  private OncePerRequestFilter roleHeaderFilter() {
    return new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response,
                                      FilterChain filterChain) throws ServletException, IOException {
        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader != null && !roleHeader.isBlank()) {
          String role = roleHeader.trim().toUpperCase();
          var auth = new UsernamePasswordAuthenticationToken(
              request.getHeader("X-User-Name"),
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + role)));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
      }
    };
  }
}

