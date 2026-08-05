package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.AdminMenu;
import com.jianghu.ling.admin.mapper.AdminMenuMapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final AdminMenuMapper adminMenuMapper;
    private final AdminRbacService adminRbacService;
    private final AuditService auditService;

    public List<Map<String, Object>> treeForCurrentUser() {
        Long adminId = AuthContext.requireAdminId();
        Set<String> perms = adminRbacService.rawPermissionCodes(adminId);
        boolean all = perms.contains("*");
        List<AdminMenu> allMenus = loadActiveMenus();
        List<AdminMenu> visible = new ArrayList<>();
        for (AdminMenu m : allMenus) {
            if (!Boolean.TRUE.equals(m.getVisible()) && m.getVisible() != null) {
                // still include if visible null treated as true
            }
            if (m.getVisible() != null && !m.getVisible()) {
                continue;
            }
            String code = m.getPermissionCode();
            if (!StringUtils.hasText(code) || all || perms.contains(code)) {
                visible.add(m);
            }
        }
        return buildTree(visible, 0L);
    }

    public List<Map<String, Object>> treeAll() {
        return buildTree(loadActiveMenus(), 0L);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        AdminMenu menu = new AdminMenu();
        apply(menu, body, true);
        if (!StringUtils.hasText(menu.getName()) || !StringUtils.hasText(menu.getType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "name/type必填");
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(true);
        }
        if (!StringUtils.hasText(menu.getStatus())) {
            menu.setStatus("ACTIVE");
        }
        adminMenuMapper.insert(menu);
        auditService.log("MENU_CREATE", "id=" + menu.getId());
        return toNode(menu, List.of());
    }

    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> body) {
        AdminMenu menu = adminMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        apply(menu, body, false);
        adminMenuMapper.updateById(menu);
        auditService.log("MENU_UPDATE", "id=" + id);
        return toNode(menu, List.of());
    }

    @Transactional
    public void delete(Long id) {
        AdminMenu menu = adminMenuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        long children = adminMenuMapper.selectCount(new LambdaQueryWrapper<AdminMenu>()
                .eq(AdminMenu::getParentId, id));
        if (children > 0) {
            throw new BizException(ErrorCode.BIZ_RULE, "存在子节点不可删除");
        }
        adminMenuMapper.deleteById(id);
        auditService.log("MENU_DELETE", "id=" + id);
    }

    private List<AdminMenu> loadActiveMenus() {
        return adminMenuMapper.selectList(new LambdaQueryWrapper<AdminMenu>()
                .eq(AdminMenu::getStatus, "ACTIVE")
                .orderByAsc(AdminMenu::getSort)
                .orderByAsc(AdminMenu::getId));
    }

    private List<Map<String, Object>> buildTree(List<AdminMenu> menus, Long parentId) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        menus.stream()
                .filter(m -> parentId.equals(m.getParentId() == null ? 0L : m.getParentId()))
                .sorted(Comparator.comparing(AdminMenu::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AdminMenu::getId))
                .forEach(m -> {
                    List<Map<String, Object>> children = buildTree(menus, m.getId());
                    // DIR 无可见子节点则不展示给当前用户
                    if ("DIR".equals(m.getType()) && children.isEmpty() && parentIdEqualsFilter(menus, m)) {
                        // 若全量树不过滤；treeForCurrentUser 时 DIR 无子则跳过
                        if (!hasAnyDescendantInList(menus, m.getId())) {
                            // keep empty dir in all-tree; for filtered list skip empty dirs
                        }
                    }
                    nodes.add(toNode(m, children));
                });
        return nodes;
    }

    private boolean parentIdEqualsFilter(List<AdminMenu> menus, AdminMenu m) {
        return true;
    }

    private boolean hasAnyDescendantInList(List<AdminMenu> menus, Long id) {
        return menus.stream().anyMatch(x -> id.equals(x.getParentId()));
    }

    /** 当前用户树：去掉无子节点的空目录 */
    public List<Map<String, Object>> treeForCurrentUserPruned() {
        List<Map<String, Object>> tree = treeForCurrentUser();
        return pruneEmptyDirs(tree);
    }

    private List<Map<String, Object>> pruneEmptyDirs(List<Map<String, Object>> nodes) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> n : nodes) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) n.get("children");
            List<Map<String, Object>> pruned = children == null ? List.of() : pruneEmptyDirs(children);
            n.put("children", pruned);
            if ("DIR".equals(n.get("type")) && pruned.isEmpty()) {
                continue;
            }
            out.add(n);
        }
        return out;
    }

    private void apply(AdminMenu menu, Map<String, Object> body, boolean creating) {
        if (body == null) {
            return;
        }
        if (body.containsKey("parentId") || creating) {
            Object pid = body.get("parentId");
            menu.setParentId(pid == null ? 0L : Long.parseLong(String.valueOf(pid)));
        }
        if (body.containsKey("type") || creating) {
            menu.setType(str(body.get("type")));
        }
        if (body.containsKey("name") || creating) {
            menu.setName(str(body.get("name")));
        }
        if (body.containsKey("path")) {
            menu.setPath(str(body.get("path")));
        }
        if (body.containsKey("component")) {
            menu.setComponent(str(body.get("component")));
        }
        if (body.containsKey("icon")) {
            menu.setIcon(str(body.get("icon")));
        }
        if (body.containsKey("sort")) {
            menu.setSort(asInt(body.get("sort")));
        }
        if (body.containsKey("visible")) {
            Object v = body.get("visible");
            menu.setVisible(v instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(v)));
        }
        if (body.containsKey("permissionCode")) {
            menu.setPermissionCode(str(body.get("permissionCode")));
        }
        if (body.containsKey("status")) {
            menu.setStatus(str(body.get("status")));
        }
    }

    private Map<String, Object> toNode(AdminMenu m, List<Map<String, Object>> children) {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", m.getId());
        n.put("parentId", m.getParentId() == null ? 0 : m.getParentId());
        n.put("type", m.getType());
        n.put("name", m.getName());
        n.put("path", m.getPath() == null ? "" : m.getPath());
        n.put("component", m.getComponent() == null ? "" : m.getComponent());
        n.put("icon", m.getIcon() == null ? "" : m.getIcon());
        n.put("sort", m.getSort());
        n.put("visible", m.getVisible() == null || m.getVisible());
        n.put("permissionCode", m.getPermissionCode());
        n.put("children", children == null ? List.of() : children);
        return n;
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private int asInt(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}
