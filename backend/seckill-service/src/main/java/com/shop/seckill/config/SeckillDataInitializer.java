package com.shop.seckill.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.seckill.entity.SeckillActivity;
import com.shop.seckill.mapper.SeckillActivityMapper;
import com.shop.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillDataInitializer implements ApplicationRunner {

    private final SeckillActivityMapper activityMapper;
    private final SeckillService seckillService;

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> activities = activityMapper.selectList(new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getStatus, "ONLINE")
                .le(SeckillActivity::getStartTime, now)
                .ge(SeckillActivity::getEndTime, now));
        activities.forEach(activity -> seckillService.preload(activity.getId()));
        if (!activities.isEmpty()) {
            log.info("Seckill stock initialized for {} activities", activities.size());
        }
    }
}

