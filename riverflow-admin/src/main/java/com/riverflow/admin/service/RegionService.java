package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.Region;

import java.util.List;

public interface RegionService extends IService<Region> {
    List<Region> buildRegionTree();
}
