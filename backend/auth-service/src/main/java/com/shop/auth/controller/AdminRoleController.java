package com.shop.auth.controller;

import com.shop.auth.dto.MenuVO;
import com.shop.auth.dto.RoleVO;
import com.shop.auth.service.AdminService;
import com.shop.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminService adminService;

    @GetMapping("/roles")
    public Result<List<RoleVO>> roles() {
        return Result.ok(adminService.listRoles());
    }

    @PostMapping("/roles")
    public Result<Void> saveRole(@RequestBody RoleVO vo) {
        adminService.saveRole(vo);
        return Result.ok();
    }

    @PutMapping("/roles/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody RoleVO vo) {
        adminService.updateRole(id, vo);
        return Result.ok();
    }

    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return Result.ok();
    }

    @GetMapping("/menus/tree")
    public Result<List<MenuVO>> menuTree() {
        return Result.ok(adminService.menuTree());
    }

    @PostMapping("/menus")
    public Result<Void> saveMenu(@RequestBody MenuVO vo) {
        adminService.saveMenu(vo);
        return Result.ok();
    }

    @PutMapping("/menus/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody MenuVO vo) {
        adminService.updateMenu(id, vo);
        return Result.ok();
    }

    @DeleteMapping("/menus/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        adminService.deleteMenu(id);
        return Result.ok();
    }
}

