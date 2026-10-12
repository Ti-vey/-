package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.SysRole;
import com.warehouse.entity.SysUser;
import com.warehouse.service.SysRoleService;
import com.warehouse.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户与角色管理控制器，仅系统管理员使用。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;
    private final SysRoleService roleService;

    /** 用户分页查询 */
    @GetMapping("/user/page")
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status) {
        IPage<SysUser> page = userService.pageQuery(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 角色下拉列表 */
    @GetMapping("/role/list")
    public Result<List<SysRole>> roles() {
        return Result.success(roleService.list());
    }

    /** 新增用户 */
    @PostMapping("/user")
    public Result<Long> save(@RequestBody SysUser user,
                             @RequestParam(required = false) Long roleId) {
        userService.saveUser(user, roleId);
        return Result.success(user.getId());
    }

    /** 修改用户 */
    @PutMapping("/user")
    public Result<Void> update(@RequestBody SysUser user,
                               @RequestParam(required = false) Long roleId) {
        userService.updateUser(user, roleId);
        return Result.success();
    }

    /** 重置密码为 123456 */
    @PutMapping("/user/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    /** 启用 / 禁用账号 */
    @PutMapping("/user/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return Result.success();
    }

    /** 删除用户 */
    @DeleteMapping("/user/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
