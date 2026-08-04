package com.riverflow.gateway.route;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的动态路由仓库
 *
 * <p>网关启动时从 gateway_route 表加载全部启用路由；运行期通过
 * {@link #refresh()} 发布 {@link RefreshRoutesEvent} 热刷新，无需重启。</p>
 */
@Slf4j
@Component
public class DbRouteDefinitionRepository implements RouteDefinitionRepository, ApplicationEventPublisherAware {

    @Resource
    private GatewayRouteMapper routeMapper;

    @Resource
    private Environment environment;

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 启动时（或刷新时）加载全部启用路由
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<GatewayRoute> routes = routeMapper.selectList(
                new QueryWrapper<GatewayRoute>().eq("enabled", 1).orderByAsc("route_order"));
        List<RouteDefinition> definitions = new ArrayList<>(routes.size());
        for (GatewayRoute route : routes) {
            try {
                definitions.add(toRouteDefinition(route));
            } catch (Exception e) {
                log.error("[Gateway] 加载路由 [{}] 失败，已跳过: {}", route.getRouteId(), e.getMessage());
            }
        }
        log.info("[Gateway] 从数据库加载路由 {} 条: {}", definitions.size(),
                definitions.stream().map(RouteDefinition::getId).toArray());
        return Flux.fromIterable(definitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            upsert(toDO(r));
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        }).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            routeMapper.delete(new QueryWrapper<GatewayRoute>().eq("route_id", id));
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        }).then();
    }

    /**
     * 供管理端显式调用：重新加载数据库路由并热生效
     */
    public void refresh() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 按 routeId 新增或更新路由（不落 RefreshRoutesEvent，由调用方决定何时刷新）
     */
    public void upsert(GatewayRoute route) {
        GatewayRoute existing = routeMapper.selectOne(
                new QueryWrapper<GatewayRoute>().eq("route_id", route.getRouteId()));
        if (existing != null) {
            route.setId(existing.getId());
            routeMapper.updateById(route);
        } else {
            if (route.getEnabled() == null) {
                route.setEnabled(1);
            }
            if (route.getRouteOrder() == null) {
                route.setRouteOrder(0);
            }
            routeMapper.insert(route);
        }
    }

    private RouteDefinition toRouteDefinition(GatewayRoute route) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(route.getRouteId());
        // 支持环境变量占位符，如 ${RIVERFLOW_ADMIN_URL:http://localhost:8080}
        String uri = environment.resolvePlaceholders(route.getUri());
        definition.setUri(URI.create(uri));
        definition.setOrder(route.getRouteOrder() == null ? 0 : route.getRouteOrder());
        if (StringUtils.hasText(route.getPredicates())) {
            definition.setPredicates(JSON.parseArray(route.getPredicates(), PredicateDefinition.class));
        }
        if (StringUtils.hasText(route.getFilters())) {
            definition.setFilters(JSON.parseArray(route.getFilters(), FilterDefinition.class));
        }
        return definition;
    }

    private GatewayRoute toDO(RouteDefinition definition) {
        GatewayRoute route = new GatewayRoute();
        route.setRouteId(definition.getId());
        route.setUri(definition.getUri().toString());
        route.setRouteOrder(definition.getOrder());
        route.setPredicates(JSON.toJSONString(definition.getPredicates()));
        route.setFilters(JSON.toJSONString(definition.getFilters()));
        route.setEnabled(1);
        return route;
    }
}
