package com.shop.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.auth.dto.MenuVO;
import com.shop.auth.dto.RoleVO;
import com.shop.auth.entity.SysMenu;
import com.shop.auth.entity.SysRole;
import com.shop.auth.entity.SysRoleMenu;
import com.shop.auth.entity.SysUser;
import com.shop.auth.mapper.SysMenuMapper;
import com.shop.auth.mapper.SysRoleMapper;
import com.shop.auth.mapper.SysRoleMenuMapper;
import com.shop.auth.mapper.SysUserMapper;
import com.shop.common.exception.BizException;
import com.shop.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final AuthService authService;

    public PageResult<SysUser> pageUsers(long current, long size, String keyword) {
        Page<SysUser> page = userMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysUser>()
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(SysUser::getUsername, keyword)
                                .or().like(SysUser::getNickname, keyword)
                                .or().like(SysUser::getPhone, keyword))
                        .orderByDesc(SysUser::getCreateTime));
        return PageResult.of(page);
    }

    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional
    public void updateUserRole(Long userId, List<Long> roleIds) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        authService.updateRoles(userId, roleIds);
    }

    public List<RoleVO> listRoles() {
        List<SysRole> roles = roleMapper.selectList(null);
        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            vo.setId(role.getId());
            vo.setCode(role.getCode());
            vo.setName(role.getName());
            vo.setDescription(role.getDescription());
            vo.setMenuIds(roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                            .eq(SysRoleMenu::getRoleId, role.getId()))
                    .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void saveRole(RoleVO vo) {
        SysRole role = new SysRole();
        role.setCode(vo.getCode());
        role.setName(vo.getName());
        role.setDescription(vo.getDescription());
        roleMapper.insert(role);
        saveRoleMenus(role.getId(), vo.getMenuIds());
    }

    @Transactional
    public void updateRole(Long id, RoleVO vo) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }
        role.setCode(vo.getCode());
        role.setName(vo.getName());
        role.setDescription(vo.getDescription());
        roleMapper.updateById(role);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        saveRoleMenus(id, vo.getMenuIds());
    }

    @Transactional
    public void deleteRole(Long id) {
        if ("SUPER_ADMIN".equals(roleMapper.selectById(id).getCode())) {
            throw new BizException("超级管理员角色不可删除");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null) {
            return;
        }
        menuIds.forEach(menuId -> {
            SysRoleMenu link = new SysRoleMenu();
            link.setRoleId(roleId);
            link.setMenuId(menuId);
            roleMenuMapper.insert(link);
        });
    }

    public List<MenuVO> menuTree() {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        Map<Long, MenuVO> voMap = menus.stream().collect(Collectors.toMap(SysMenu::getId, m -> {
            MenuVO vo = new MenuVO();
            vo.setId(m.getId());
            vo.setParentId(m.getParentId());
            vo.setName(m.getName());
            vo.setPath(m.getPath());
            vo.setIcon(m.getIcon());
            vo.setPermissionCode(m.getPermissionCode());
            vo.setType(m.getType());
            vo.setSort(m.getSort());
            return vo;
        }));
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : voMap.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                MenuVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }

    @Transactional
    public void saveMenu(MenuVO vo) {
        SysMenu menu = new SysMenu();
        applyMenu(menu, vo);
        menuMapper.insert(menu);
    }

    @Transactional
    public void updateMenu(Long id, MenuVO vo) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(404, "菜单不存在");
        }
        applyMenu(menu, vo);
        menuMapper.updateById(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        menuMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id));
    }

    private void applyMenu(SysMenu menu, MenuVO vo) {
        menu.setParentId(vo.getParentId() == null ? 0 : vo.getParentId());
        menu.setName(vo.getName());
        menu.setPath(vo.getPath());
        menu.setIcon(vo.getIcon());
        menu.setPermissionCode(vo.getPermissionCode());
        menu.setType(vo.getType() == null ? "MENU" : vo.getType());
        menu.setSort(vo.getSort() == null ? 0 : vo.getSort());
    }

}
