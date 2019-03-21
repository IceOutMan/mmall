package com.mmall.controler.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        log.info("preHandle");

        //请求中Controler中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) o;

        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        //解析参数
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = httpServletRequest.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String mapKey = (String) entry.getKey();

            String mapValue  = "";

            //request这个参数的Map，里买的value返回的是一个String[]
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        if (StringUtils.equals(className,"UserManagerController") && StringUtils.equals(methodName,"login")) {
            log.info("权限拦截起拦截到请求,className:{},methodName:{}",className,methodName);
            //如果是拦截到登录请求，不打印参数，因为参数里面是账号密码，会全部打印到日志中，防止日志泄漏
            return true;
        }


        User user = null;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr,User.class);
        }

        //上传由于富文本的控件要求，要特殊处理返回值，这里面区分是否登录以及 是否有权限
        if (user == null || (user.getRole().intValue()!= Const.Role.ROLE_ADMIN)) {
            //返回false 既不会调用controller里的方法
            httpServletResponse.reset();//这里要添加reset 否则的话会报异常，getWriter（） has alreadly been called for this response
            httpServletResponse.setCharacterEncoding("UTF-8");//这里要设置编码，否则就会乱码
            httpServletResponse.setContentType("application/json;charset=UTF-8");//这里要设置返回值的类型，因为全部是json格式的接口

            PrintWriter out = httpServletResponse.getWriter();

            if (user == null) {
                if (StringUtils.equals(className,"UserManagerController") && StringUtils.equals(methodName,"login")) {
                    Map resutltMap = Maps.newHashMap();
                    resutltMap.put("success",false);
                    resutltMap.put("msg","请登录管理员");
                    out.print(JsonUtil.obj2String(resutltMap));
                }else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("用户未登录")));
                }
            }else{
                if (StringUtils.equals(className,"UserManagerController") && StringUtils.equals(methodName,"login")) {
                    Map resutltMap = Maps.newHashMap();
                    resutltMap.put("success",false);
                    resutltMap.put("msg","无权限操作");
                    out.print(JsonUtil.obj2String(resutltMap));
                }else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("用户没有权限操作")));
                }
            }
            out.flush();
            out.close();//这里是要关闭的

            return false;
        }

        return true;//false 就不会进入到controler里面去，下面的方法也不会执行的
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        log.info("afterCompletion");

    }
}
