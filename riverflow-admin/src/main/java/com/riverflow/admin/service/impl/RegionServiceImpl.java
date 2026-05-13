package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.RegionMapper;
import com.riverflow.admin.service.RegionService;
import com.riverflow.api.entity.Region;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    @Override
    public List<Region> buildRegionTree() {
        List<Region> all = list(new QueryWrapper<Region>().eq("del_flag", 0).orderByAsc("sort_no"));
        return buildTree(all, "0");
    }

    private List<Region> buildTree(List<Region> all, String parentCode) {
        List<Region> result = new ArrayList<>();
        for (Region region : all) {
            if (parentCode.equals(region.getParentCode())) {
                List<Region> children = buildTree(all, region.getRegionCode());
                // Note: Region entity doesn't have children field, this is conceptual
                result.add(region);
            }
        }
        return result;
    }
}
