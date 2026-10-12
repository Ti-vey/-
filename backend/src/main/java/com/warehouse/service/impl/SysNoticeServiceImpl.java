package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.SysNotice;
import com.warehouse.mapper.SysNoticeMapper;
import com.warehouse.service.SysNoticeService;
import org.springframework.stereotype.Service;

/** 公告业务实现 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {
}
