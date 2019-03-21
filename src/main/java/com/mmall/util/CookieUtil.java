package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

    private final static String COOKIE_DOMIN = "localhost";
    private final static String COOKIE_NAME = "mmall_login_token";



    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();

        if (cks != null) {
            for (Cookie ck : cks) {
                log.info("cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if (StringUtils.equals(ck.getName(),COOKIE_NAME)) {
                    log.info("return cookiename:{},cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
                
            }
        }
        return null;
    }

    public static void writeLoginToken(HttpServletResponse response,String token){
        Cookie ck = new Cookie(COOKIE_NAME,token);
        ck.setDomain(COOKIE_DOMIN);
        ck.setPath("/");//这个代表设置在根目录
        ck.setHttpOnly(true);//不许通过脚本访问cookie
        //单位是s，如果这个maxage不设置的话，cookie就不会写入硬盘，而是写在内存中，只在当前页面有效
        ck.setMaxAge(60 * 60 * 24 * 365);//如果是-1代表的是永久
        log.info("write cookieName: {},cookieValue:{}",ck.getName(),ck.getValue());
        response.addCookie(ck);


    }

    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                if (StringUtils.equals(ck.getName(),COOKIE_NAME)) {
                    ck.setDomain(COOKIE_DOMIN);
                    ck.setPath("/");
                    ck.setMaxAge(0);//设置为0，代表删除此Cookie
                    log.info("del cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }

    }

}
