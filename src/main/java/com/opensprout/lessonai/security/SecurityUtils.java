package com.opensprout.lessonai.security;

import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.UserDO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static UserDO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDO)) {
            throw new BusinessException("登录状态已失效");
        }
        return (UserDO) authentication.getPrincipal();
    }

}
