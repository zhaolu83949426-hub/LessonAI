package com.opensprout.lessonai.security;

import com.opensprout.lessonai.domain.entity.UserDO;
import com.opensprout.lessonai.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Claims claims = jwtTokenProvider.parseToken(token);
                Long userId = Long.valueOf(claims.getSubject());
                UserDO user = userService.getById(userId);
                if (user != null && Boolean.FALSE.equals(user.getDeleted())) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            AuthorityUtils.createAuthorityList("ROLE_USER"));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception ignored) {
            }
        }
        filterChain.doFilter(request, response);
    }

}
