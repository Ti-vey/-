package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.SysLog;

/** 操作日志业务接口 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 记录一条操作日志。
     *
     * @param username 操作人账号，登录接口在写入上下文之前调用，因此需要显式传入
     */
    void record(String username, String module, String operation, String method, String params, Long costTime);
}
