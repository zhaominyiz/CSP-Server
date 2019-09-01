package com.njust.csa.reg.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getSession().getAttribute("username") == null && request.getSession().getAttribute("isRedirect") == null){
            response.sendRedirect("login");
            return false;
        }
        request.getSession().removeAttribute("isRedirect");
        return true;
    }
}
