package com.shop.seckill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.api.dto.PromotionDTO;
import com.shop.api.dto.SeckillOrderRequest;
import com.shop.api.dto.SkuDTO;
import com.shop.api.feign.ProductClient;
import com.shop.common.cache.RedisCache;
import com.shop.common.exception.BizException;
import com.shop.common.mq.MQConstants;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.common.util.OrderNoGenerator;
import com.shop.seckill.dto.ActivityForm;
import com.shop.seckill.dto.ActivityVO;
import com.shop.seckill.dto.SeckillResult;
import com.shop.seckill.entity.SeckillActivity;
import com.shop.seckill.entity.SeckillProduct;
import com.shop.seckill.entity.SeckillRecord;
import com.shop.seckill.mapper.SeckillActivityMapper;
import com.shop.seckill.mapper.SeckillProductMapper;
import com.shop.seckill.mapper.SeckillRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USER_KEY_PREFIX = "seckill:user:";
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end\n" +
                    "local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')\n" +
                    "if stock <= 0 then return 0 end\n" +
                    "redis.call('DECR', KEYS[1])\n" +
                    "redis.call('SET', KEYS[2], '1', 'EX', 86400)\n" +
                    "return 1", Long.class);

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper productMapper;
    private final SeckillRecordMapper recordMapper;
    private final ProductClient productClient;
    private final RedisCache redisCache;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PageResult<ActivityVO> listActivities(long current, long size, String type) {
        LocalDateTime now = LocalDateTime.now();
        Page<SeckillActivity> page = activityMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, "ONLINE")
                        .le(SeckillActivity::getStartTime, now)
                        .ge(SeckillActivity::getEndTime, now)
                        .eq(type != null && !type.isBlank(), SeckillActivity::getType, type)
                        .orderByDesc(SeckillActivity::getStartTime));
        return PageResult.of(page.getRecords().stream().map(a -> toVO(a, false)).collect(Collectors.toList()),
                page.getTotal(), page.getCurrent(), page.getSize());
    }

    public ActivityVO detail(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        return toVO(activity, false);
    }

    public List<ActivityVO> adminActivities(long current, long size, String type, String status) {
        Page<SeckillActivity> page = activityMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(type != null && !type.isBlank(), SeckillActivity::getType, type)
                        .eq(status != null && !status.isBlank(), SeckillActivity::getStatus, status)
                        .orderByDesc(SeckillActivity::getCreateTime));
        return page.getRecords().stream().map(a -> toVO(a, true)).collect(Collectors.toList());
    }

    public ActivityVO adminDetail(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        return toVO(activity, true);
    }

    public PromotionDTO internalPromotion(String code) {
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = activityMapper.selectOne(new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getType, "PROMOTION")
                .eq(SeckillActivity::getPromotionCode, code)
                .eq(SeckillActivity::getStatus, "ONLINE")
                .le(SeckillActivity::getStartTime, now)
                .ge(SeckillActivity::getEndTime, now)
                .last("LIMIT 1"));
        if (activity == null) {
            return null;
        }
        PromotionDTO dto = new PromotionDTO();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setDiscountType(activity.getDiscountType());
        dto.setDiscountValue(activity.getDiscountValue());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        return dto;
    }

    public SeckillResult seckill(Long userId, Long activityId, Long seckillProductId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null || !"ONLINE".equals(activity.getStatus())) {
            throw new BizException("活动不在进行中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BizException("活动未开始或已结束");
        }
        SeckillProduct seckillProduct = productMapper.selectById(seckillProductId);
        if (seckillProduct == null || !activityId.equals(seckillProduct.getActivityId())
                || seckillProduct.getStatus() == null || seckillProduct.getStatus() != 1) {
            throw new BizException(404, "秒杀商品不存在");
        }

        String stockKey = STOCK_KEY_PREFIX + seckillProductId;
        if (!redisCache.hasKey(stockKey)) {
            redisCache.set(stockKey, String.valueOf(seckillProduct.getSeckillStock()), 0);
        }
        String userKey = USER_KEY_PREFIX + activityId + ":" + userId;
        Long allowed = (Long) redisCache.execute(SECKILL_SCRIPT, List.of(stockKey, userKey));

        SeckillResult result = new SeckillResult();
        if (allowed == null || allowed != 1) {
            if (Boolean.TRUE.equals(redisCache.hasKey(userKey))) {
                result.setSuccess(false);
                result.setMessage("您已参与过该秒杀活动");
            } else {
                result.setSuccess(false);
                result.setMessage("手慢了，商品已抢完");
            }
            return result;
        }

        String orderNo = OrderNoGenerator.generate("S");
        SeckillRecord record = null;
        try {
            record = new SeckillRecord();
            record.setActivityId(activityId);
            record.setSeckillProductId(seckillProductId);
            record.setSkuId(seckillProduct.getSkuId());
            record.setUserId(userId);
            record.setOrderNo(orderNo);
            record.setQuantity(1);
            record.setStatus("SUCCESS");
            recordMapper.insert(record);
        } catch (Exception e) {
            redisCache.increment(stockKey);
            redisCache.delete(userKey);
            result.setSuccess(false);
            result.setMessage("您已参与过该秒杀活动");
            return result;
        }

        boolean published = publishOrder(activityId, seckillProduct, userId, orderNo);
        if (!published) {
            redisCache.increment(stockKey);
            redisCache.delete(userKey);
            recordMapper.deleteById(record.getId());
            result.setSuccess(false);
            result.setMessage("抢购人数过多，请重试");
            return result;
        }
        result.setSuccess(true);
        result.setMessage("抢购成功，请尽快完成支付");
        result.setOrderNo(orderNo);
        return result;
    }

    @Transactional
    public void createActivity(ActivityForm form) {
        SeckillActivity activity = new SeckillActivity();
        applyActivity(activity, form);
        activityMapper.insert(activity);
        saveProducts(activity.getId(), form.getProducts());
        if ("ONLINE".equals(activity.getStatus())) {
            preload(activity.getId());
        }
    }

    @Transactional
    public void updateActivity(Long id, ActivityForm form) {
        SeckillActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        applyActivity(activity, form);
        activityMapper.updateById(activity);
        productMapper.delete(new LambdaQueryWrapper<SeckillProduct>().eq(SeckillProduct::getActivityId, id));
        saveProducts(id, form.getProducts());
        if ("ONLINE".equals(activity.getStatus())) {
            preload(id);
        }
    }

    @Transactional
    public void deleteActivity(Long id) {
        List<SeckillProduct> products = productMapper.selectList(new LambdaQueryWrapper<SeckillProduct>()
                .eq(SeckillProduct::getActivityId, id));
        products.forEach(p -> redisCache.delete(STOCK_KEY_PREFIX + p.getId()));
        productMapper.delete(new LambdaQueryWrapper<SeckillProduct>().eq(SeckillProduct::getActivityId, id));
        activityMapper.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        SeckillActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        activity.setStatus(status);
        activityMapper.updateById(activity);
        if ("ONLINE".equals(status)) {
            preload(id);
        }
    }

    public void preload(Long activityId) {
        List<SeckillProduct> products = productMapper.selectList(new LambdaQueryWrapper<SeckillProduct>()
                .eq(SeckillProduct::getActivityId, activityId)
                .eq(SeckillProduct::getStatus, 1));
        products.forEach(product -> redisCache.set(STOCK_KEY_PREFIX + product.getId(),
                String.valueOf(product.getSeckillStock()), 0));
        log.info("Seckill stock preloaded, activityId={}, products={}", activityId, products.size());
    }

    public List<SeckillProduct> adminProducts(Long activityId) {
        return productMapper.selectList(new LambdaQueryWrapper<SeckillProduct>()
                .eq(activityId != null, SeckillProduct::getActivityId, activityId)
                .orderByDesc(SeckillProduct::getCreateTime));
    }

    private void applyActivity(SeckillActivity activity, ActivityForm form) {
        activity.setName(form.getName());
        activity.setType(form.getType());
        activity.setPromotionCode(form.getPromotionCode());
        activity.setDiscountType(form.getDiscountType());
        activity.setDiscountValue(form.getDiscountValue());
        activity.setDescription(form.getDescription());
        activity.setStartTime(form.getStartTime());
        activity.setEndTime(form.getEndTime());
        activity.setStatus(form.getStatus() == null ? "DRAFT" : form.getStatus());
    }

    private void saveProducts(Long activityId, List<ActivityForm.ProductForm> forms) {
        if (forms == null) {
            return;
        }
        forms.forEach(form -> {
            SkuDTO sku = getSku(form.getSkuId());
            SeckillProduct product = new SeckillProduct();
            product.setActivityId(activityId);
            product.setSkuId(form.getSkuId());
            product.setProductId(sku.getProductId());
            product.setProductName(fetchProductName(sku.getProductId()));
            product.setImage(sku.getImage());
            product.setSeckillPrice(form.getSeckillPrice());
            product.setSeckillStock(form.getSeckillStock() == null ? 0 : form.getSeckillStock());
            product.setLimitPerUser(form.getLimitPerUser() == null ? 1 : form.getLimitPerUser());
            product.setStatus(form.getStatus() == null ? 1 : form.getStatus());
            productMapper.insert(product);
        });
    }

    private ActivityVO toVO(SeckillActivity activity, boolean includeAllProducts) {
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setName(activity.getName());
        vo.setType(activity.getType());
        vo.setPromotionCode(activity.getPromotionCode());
        vo.setDiscountType(activity.getDiscountType());
        vo.setDiscountValue(activity.getDiscountValue());
        vo.setDescription(activity.getDescription());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        List<SeckillProduct> products = productMapper.selectList(new LambdaQueryWrapper<SeckillProduct>()
                .eq(SeckillProduct::getActivityId, activity.getId())
                .eq(!includeAllProducts, SeckillProduct::getStatus, 1));
        vo.setProducts(products.stream().map(product -> {
            ActivityVO.ProductVO pvo = new ActivityVO.ProductVO();
            pvo.setId(product.getId());
            pvo.setSkuId(product.getSkuId());
            pvo.setProductId(product.getProductId());
            pvo.setProductName(product.getProductName());
            pvo.setImage(product.getImage());
            pvo.setSeckillPrice(product.getSeckillPrice());
            pvo.setSeckillStock(product.getSeckillStock());
            pvo.setRemainStock(readRemain(product.getId()));
            pvo.setLimitPerUser(product.getLimitPerUser());
            try {
                SkuDTO sku = getSku(product.getSkuId());
                pvo.setSkuSpec(sku.getSpecName() + " " + sku.getSpecValue());
            } catch (Exception ignored) {
                pvo.setSkuSpec("");
            }
            return pvo;
        }).collect(Collectors.toList()));
        return vo;
    }

    private Integer readRemain(Long seckillProductId) {
        String value = redisCache.get(STOCK_KEY_PREFIX + seckillProductId);
        return value == null ? null : Integer.valueOf(value);
    }

    private boolean publishOrder(Long activityId, SeckillProduct product, Long userId, String orderNo) {
        try {
            SkuDTO sku = getSku(product.getSkuId());
            SeckillOrderRequest request = new SeckillOrderRequest();
            request.setOrderNo(orderNo);
            request.setUserId(userId);
            request.setActivityId(activityId);
            request.setSeckillProductId(product.getId());
            request.setSkuId(product.getSkuId());
            request.setProductId(product.getProductId());
            request.setProductName(product.getProductName());
            request.setSkuSpec(sku.getSpecName() + " " + sku.getSpecValue());
            request.setImage(product.getImage());
            request.setQuantity(1);
            request.setSeckillPrice(product.getSeckillPrice());
            rabbitTemplate.convertAndSend(MQConstants.TOPIC_EXCHANGE, MQConstants.KEY_SECKILL_ORDER_CREATED,
                    objectMapper.writeValueAsString(request));
            return true;
        } catch (Exception e) {
            log.error("publish seckill order failed, orderNo={}", orderNo, e);
            return false;
        }
    }

    private SkuDTO getSku(Long skuId) {
        try {
            Result<SkuDTO> result = productClient.getSku(skuId);
            if (!result.isSuccess() || result.getData() == null) {
                throw new BizException(404, "SKU不存在");
            }
            return result.getData();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("商品服务暂时不可用");
        }
    }

    private String fetchProductName(Long productId) {
        try {
            var result = productClient.getProduct(productId);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData().getName();
            }
        } catch (Exception ignored) {
            // fallback
        }
        return "秒杀商品";
    }
}
