package com.zhumingbei.techblog.config;

import com.zhumingbei.techblog.bean.UserBean;
import com.zhumingbei.techblog.common.CustomUserPrincipal;
import com.zhumingbei.techblog.constant.SessionConstant;
import com.zhumingbei.techblog.service.UserService;
import com.zhumingbei.techblog.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class RbacAuthorityConfig {
    @Autowired
    private UserService userService;
    @Autowired
    private NonePermissionUrlConfig urlConfig;
    //对登陆用户的权限管理，不需要登陆的权限直接放行
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (checkRequest(request)) {
            return true;
        }else if (principal instanceof CustomUserPrincipal) {
            return verify(request, (CustomUserPrincipal) principal);
        }
        log.debug("anonymous user cannot visit the url without permission");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CustomUserPrincipal.createGuest(), null)
        );
        return false;


    }

    private boolean verify(HttpServletRequest request, CustomUserPrincipal userPrincipal) {

        Set<String> urls = new HashSet<>();
        for (GrantedAuthority authority : userPrincipal.getAuthorities()){
            urls.add(authority.getAuthority());
        }
        log.debug("rbac permission urls: " + urls);
        for (String url : urls) {
            AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(url);
            if (antPathRequestMatcher.matches(request)) {
                log.debug("rbac permission success");

                return true;
            }
        }
        log.debug("user doesn't have the permission");
        return false;
    }

    private boolean hasUsedToken(HttpServletRequest request) {
        String token = request.getHeader(SessionConstant.HEADER_SESSION_TOKEN);

        if (token == null) {
            return false;
        }else {
            UserBean user = userService.findByToken(token);
            if (user != null) {
                return true;
            }
            return false;
        }
    }

    private boolean checkRequest(HttpServletRequest request) {
        HashSet<String> set = new HashSet<>();
        set.addAll(urlConfig.getGet());
        set.addAll(urlConfig.getPost());
        set.addAll(urlConfig.getPattern());
        for (String url : set) {
            AntPathRequestMatcher matcher = new AntPathRequestMatcher(url);
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }
}
