package com.midnight.rpc.core.cluster;

import com.midnight.rpc.core.api.Router;
import com.midnight.rpc.core.meta.InstanceMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {
    private int grayRatio;
    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        for (InstanceMeta provider : providers) {
            if ("true".equals(provider.getParameters().get("gray"))) {
                grayNodes.add(provider);
            } else {
                normalNodes.add(provider);
            }
        }

        log.debug(" grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}",
                grayNodes.size(), normalNodes.size(), grayRatio);

        if (grayNodes.isEmpty() || normalNodes.isEmpty()) {
            return providers;
        }

        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100) {
            return grayNodes;
        }

        if (random.nextInt(100) < grayRatio) {
            log.debug(" grayRouter grayNodes ===> {}", grayNodes);
            return grayNodes;
        } else {
            log.debug(" grayRouter normalNodes ===> {}", normalNodes);
            return normalNodes;
        }
    }
}
