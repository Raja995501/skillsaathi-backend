package com.skillsaathi.util;

import com.skillsaathi.exception.UnauthorizedException;
import com.skillsaathi.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper so controllers/services don't repeat SecurityContext plumbing
 * every time they need "who is the currently authenticated user".
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new UnauthorizedException("No authenticated user found in context");
        }
        return userDetails.getId();
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new UnauthorizedException("No authenticated user found in context");
        }
        return userDetails.getUsername();
    }
}
