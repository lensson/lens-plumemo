package com.byteblogs.system.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.byteblogs.common.annotation.OperateLog;
import com.byteblogs.common.constant.Constants;
import com.byteblogs.common.util.HttpContextUtils;
import com.byteblogs.common.util.JsonUtil;
import com.byteblogs.helloblog.auth.domain.po.AuthUser;
import com.byteblogs.helloblog.log.domain.vo.HelloBlogAuthUserLogVO;
import com.byteblogs.system.sync.LogSyncTask;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Optional;

@Aspect
@Component
public class LogAspect {
    @Autowired
    private LogSyncTask logSyncTask;
    @Pointcut(value = "@annotation(com.byteblogs.common.annotation.OperateLog)")
    public void logsPointCut() {}// 配置切点

    /**
     * 配置环绕通知
     */
    @Around("logsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis(); // 开始时间
        Object result = point.proceed(); // 执行方法
        long time = System.currentTimeMillis() - beginTime;// 结束时间
        this.saveLog(point, time);
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long time){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HelloBlogAuthUserLogVO helloBlogAuthUserLogVO = new HelloBlogAuthUserLogVO();
        OperateLog operateLog = method.getAnnotation(OperateLog.class);
        if (operateLog != null) {
            helloBlogAuthUserLogVO.setDescription(operateLog.module());//  获取注解上的描述
            helloBlogAuthUserLogVO.setCode(operateLog.code().getCode());// 获取主街上定义的类型
        }

        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        // 请求的参数
        Enumeration<String> enums = request.getParameterNames();
        JSONObject parameter =new JSONObject();
        while(enums.hasMoreElements()) {
            String paramName = enums.nextElement();
            String paramValue = request.getParameter(String.valueOf(paramName));
            parameter.put(paramName,paramValue);
        }
        helloBlogAuthUserLogVO.setParamter(parameter.toJSONString());
        helloBlogAuthUserLogVO.setIp(HttpContextUtils.getIpAddr(request));
        helloBlogAuthUserLogVO.setUrl(request.getRequestURI());
        helloBlogAuthUserLogVO.setDevice(HttpContextUtils.getOsName(request));
        helloBlogAuthUserLogVO.setBrowserName(HttpContextUtils.getBrowserName(request));
        helloBlogAuthUserLogVO.setBrowserVersion(HttpContextUtils.getBrowserVersion(request));

        // 取得用户信息
        String token = Optional.ofNullable(request.getHeader(Constants.AUTHENTICATION)).orElse(null);
        if (!StringUtils.isBlank(token)) {
            AuthUser authUser= JsonUtil.parseObject(JWT.decode(token).getAudience().get(0), AuthUser.class);
            helloBlogAuthUserLogVO.setUserId(authUser.getId().toString());
        }else{
            helloBlogAuthUserLogVO.setUserId(Constants.DEFAULT_USERID);
        }
        // 统计时间
        helloBlogAuthUserLogVO.setRunTime(time);
        helloBlogAuthUserLogVO.setCreateTime(LocalDateTime.now());
        // 保存系统日志
        logSyncTask.addLog(helloBlogAuthUserLogVO);
    }
}