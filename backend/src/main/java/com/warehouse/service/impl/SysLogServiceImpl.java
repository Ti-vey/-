package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.SysLog;
import com.warehouse.mapper.SysLogMapper;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/** 操作日志业务实现 */
@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    private static final int MAX_PARAM_LENGTH = 2000;

    @Override
    public void record(String username, String module, String operation, String method, String params, Long costTime) {
        try {
            SysLog entity = new SysLog();
            LoginUser loginUser = UserContext.get();
            if (loginUser != null) {
                entity.setUserId(loginUser.getUserId());
            }
            entity.setUsername(username != null ? username : (loginUser == null ? null : loginUser.getUsername()));
            entity.setModule(module);
            entity.setOperation(operation);
            entity.setMethod(method);
            entity.setParams(truncate(params));
            entity.setCostTime(costTime);
            entity.setIp(currentIp());
            this.save(entity);
        } catch (Exception e) {
            // 日志写入失败不应影响正常业务，这里仅记录到控制台
            log.warn("操作日志写入失败：{}", e.getMessage());
        }
    }

    private String truncate(String params) {
        if (params == null) {
            return null;
        }
        return params.length() > MAX_PARAM_LENGTH ? params.substring(0, MAX_PARAM_LENGTH) : params;
    }

    private String currentIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        return ip == null ? request.getRemoteAddr() : ip;
    }
}
