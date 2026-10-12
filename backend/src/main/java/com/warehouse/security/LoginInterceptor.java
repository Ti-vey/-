package com.warehouse.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.common.Result;
import com.warehouse.common.ResultCode;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 登录认证拦截器：校验请求头中的 JWT，并把用户信息写入线程上下文。
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(TOKEN_PREFIX)) {
            writeUnauthorized(response, "请先登录后再访问");
            return false;
        }
        String token = header.substring(TOKEN_PREFIX.length());
        try {
            LoginUser loginUser = jwtUtil.parseToken(token);
            if (loginUser.getUserId() == null) {
                writeUnauthorized(response, "登录信息不完整，请重新登录");
                return false;
            }
            UserContext.set(loginUser);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), msg);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
