package com.riverflow.gateway.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.common.result.R;
import com.riverflow.gateway.route.DbRouteDefinitionRepository;
import com.riverflow.gateway.route.GatewayRoute;
import com.riverflow.gateway.route.GatewayRouteMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 网关路由管理端点（内部接口）
 *
 * <p>注意：该组接口由 JwtAuthGlobalFilter 强制要求有效 JWT，
 * 生产环境应仅允许内网或管理员访问，避免外部直接修改路由。</p>
 */
@RestController
@RequestMapping("/gateway/route")
public class RouteManagerController {

    @Resource
    private DbRouteDefinitionRepository routeRepository;

    @Resource
    private GatewayRouteMapper routeMapper;

    /**
     * 查询全部路由
     */
    @GetMapping("/list")
    public R<List<GatewayRoute>> list() {
        return R.ok(routeMapper.selectList(new QueryWrapper<GatewayRoute>().orderByAsc("route_order")));
    }

    /**
     * 新增或更新路由（按 routeId 判重），保存后立即热生效
     */
    @PostMapping("/save")
    public R<Void> save(@RequestBody GatewayRoute route) {
        routeRepository.upsert(route);
        routeRepository.refresh();
        return R.ok();
    }

    /**
     * 删除路由并热生效
     */
    @PostMapping("/delete/{routeId}")
    public R<Void> delete(@PathVariable String routeId) {
        routeMapper.delete(new QueryWrapper<GatewayRoute>().eq("route_id", routeId));
        routeRepository.refresh();
        return R.ok();
    }

    /**
     * 手动触发路由热刷新（直接改表后调用）
     */
    @PostMapping("/refresh")
    public R<Void> refresh() {
        routeRepository.refresh();
        return R.ok();
    }
}
