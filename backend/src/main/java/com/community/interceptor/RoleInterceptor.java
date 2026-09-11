package com.community.interceptor;

import com.community.common.LoginUser;
import com.community.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LoginUser user = UserContext.get();
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            throw new BusinessException(403, "无权限访问");
        }
        return true;
    }
}
