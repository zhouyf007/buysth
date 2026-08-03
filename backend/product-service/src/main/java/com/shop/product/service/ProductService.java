package com.shop.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.api.dto.ProductDTO;
import com.shop.api.dto.SalesIncreaseRequest;
import com.shop.api.dto.SkuDTO;
import com.shop.api.dto.StockLockRequest;
import com.shop.api.dto.UserDTO;
import com.shop.api.feign.AuthClient;
import com.shop.common.cache.RedisCache;
import com.shop.common.exception.BizException;
import com.shop.common.result.PageResult;
import com.shop.common.result.Result;
import com.shop.product.dto.ProductForm;
import com.shop.product.dto.ProductVO;
import com.shop.product.dto.CategorySalesStat;
import com.shop.product.entity.Category;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductSku;
import com.shop.product.entity.ProductReview;
import com.shop.product.mapper.CategoryMapper;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.mapper.ProductReviewMapper;
import com.shop.product.mapper.ProductSkuMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.math.RoundingMode;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String CATEGORY_CACHE_KEY = "product:categories";
    private static final String HOT_CACHE_KEY = "product:hot";
    private static final String DETAIL_CACHE_PREFIX = "product:detail:";

    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final CategoryMapper categoryMapper;
    private final ProductReviewMapper reviewMapper;
    private final RedisCache redisCache;
    private final AuthClient authClient;

    public List<Category> listCategories() {
        String cached = redisCache.get(CATEGORY_CACHE_KEY);
        if (cached != null) {
            List<Category> categories = redisCache.get(CATEGORY_CACHE_KEY, new TypeReference<List<Category>>() {
            });
            if (categories != null) {
                return categories;
            }
        }
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort));
        redisCache.set(CATEGORY_CACHE_KEY, categories, 3600);
        return categories;
    }

    public PageResult<ProductVO> search(long current, long size, String name, Long categoryId, String region,
                                        String brand, BigDecimal minPrice, BigDecimal maxPrice,
                                        LocalDateTime startDate, LocalDateTime endDate, String sort) {
        IPage<Product> page = productMapper.searchPage(new Page<>(current, size), name, categoryId,
                region, brand, minPrice, maxPrice, startDate, endDate, sort);
        return PageResult.of(enrichProducts(page.getRecords(), false), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public PageResult<ProductVO> adminPage(long current, long size, String keyword, Long categoryId, Integer status) {
        Page<Product> page = productMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Product>()
                        .and(StringUtils.hasText(keyword), w -> w.like(Product::getName, keyword)
                                .or().like(Product::getBrand, keyword))
                        .eq(categoryId != null, Product::getCategoryId, categoryId)
                        .eq(status != null, Product::getStatus, status)
                        .orderByDesc(Product::getCreateTime));
        return PageResult.of(enrichProducts(page.getRecords(), true), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public ProductVO detail(Long id) {
        ProductVO cached = redisCache.get(DETAIL_CACHE_PREFIX + id, new TypeReference<ProductVO>() {
        });
        if (cached != null) {
            return cached;
        }
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BizException(404, "商品不存在或已下架");
        }
        ProductVO vo = enrichProducts(List.of(product), false).get(0);
        redisCache.set(DETAIL_CACHE_PREFIX + id, vo, 1800);
        return vo;
    }

    public ProductVO adminDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        return enrichProducts(List.of(product), true).get(0);
    }

    public List<ProductVO> hot() {
        String cached = redisCache.get(HOT_CACHE_KEY);
        if (cached != null) {
            List<ProductVO> hot = redisCache.get(HOT_CACHE_KEY, new TypeReference<List<ProductVO>>() {
            });
            if (hot != null) {
                return hot;
            }
        }
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("LIMIT 8"));
        List<ProductVO> result = enrichProducts(products, false);
        redisCache.set(HOT_CACHE_KEY, result, 600);
        return result;
    }

    public ProductDTO internalProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setCategoryId(product.getCategoryId());
        dto.setName(product.getName());
        dto.setSubtitle(product.getSubtitle());
        dto.setBrand(product.getBrand());
        dto.setRegion(product.getRegion());
        dto.setMainImage(product.getMainImage());
        dto.setDetail(product.getDetail());
        dto.setSales(product.getSales());
        dto.setRating(product.getRating());
        dto.setPublishDate(product.getPublishDate());
        dto.setStatus(product.getStatus());
        return dto;
    }

    public SkuDTO internalSku(Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            return null;
        }
        SkuDTO dto = new SkuDTO();
        dto.setId(sku.getId());
        dto.setProductId(sku.getProductId());
        dto.setSpecName(sku.getSpecName());
        dto.setSpecValue(sku.getSpecValue());
        dto.setPrice(sku.getPrice());
        dto.setStock(sku.getStock());
        dto.setImage(sku.getImage());
        dto.setStatus(sku.getStatus());
        return dto;
    }

    public boolean lockStock(StockLockRequest request) {
        return skuMapper.deductStock(request.getSkuId(), request.getQuantity()) == 1;
    }

    public boolean releaseStock(StockLockRequest request) {
        return skuMapper.restoreStock(request.getSkuId(), request.getQuantity()) == 1;
    }

    public boolean increaseSales(SalesIncreaseRequest request) {
        if (request.getItems() != null) {
            request.getItems().forEach(item -> {
                productMapper.increaseSales(item.getProductId(), item.getQuantity() == null ? 0 : item.getQuantity());
                redisCache.delete(DETAIL_CACHE_PREFIX + item.getProductId());
            });
        }
        redisCache.delete(HOT_CACHE_KEY);
        return true;
    }

    @Transactional
    public void addReview(Long productId, Long userId, Integer rating, String content) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BizException(404, "商品不存在或已下架");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BizException("评分需在1-5分之间");
        }
        if (content == null || content.isBlank()) {
            throw new BizException("评价内容不能为空");
        }
        if (content.length() > 500) {
            throw new BizException("评价内容不能超过500字");
        }
        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setNickname(fetchNickname(userId));
        review.setRating(rating);
        review.setContent(content.trim());
        review.setStatus(1);
        reviewMapper.insert(review);

        java.math.BigDecimal avg = reviewMapper.averageRating(productId);
        product.setRating(avg == null ? java.math.BigDecimal.valueOf(5) : avg.setScale(1, RoundingMode.HALF_UP));
        productMapper.updateById(product);
        redisCache.delete(DETAIL_CACHE_PREFIX + productId);
    }

    public PageResult<ProductReview> pageReviews(Long productId, long current, long size) {
        Page<ProductReview> page = reviewMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<ProductReview>()
                        .eq(ProductReview::getProductId, productId)
                        .eq(ProductReview::getStatus, 1)
                        .orderByDesc(ProductReview::getCreateTime));
        return PageResult.of(page);
    }

    public List<CategorySalesStat> categorySales() {
        List<Category> categories = listCategories();
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>());
        Map<Long, CategorySalesStat> statMap = new LinkedHashMap<>();
        categories.forEach(c -> {
            CategorySalesStat stat = new CategorySalesStat();
            stat.setCategoryId(c.getId());
            stat.setCategoryName(c.getName());
            stat.setSales(0);
            stat.setProductCount(0);
            statMap.put(c.getId(), stat);
        });
        products.forEach(p -> {
            CategorySalesStat stat = statMap.get(p.getCategoryId());
            if (stat == null) {
                stat = new CategorySalesStat();
                stat.setCategoryId(p.getCategoryId());
                stat.setCategoryName("未分类");
                stat.setSales(0);
                stat.setProductCount(0);
                statMap.put(p.getCategoryId(), stat);
            }
            stat.setSales(stat.getSales() + (p.getSales() == null ? 0 : p.getSales()));
            stat.setProductCount(stat.getProductCount() + 1);
        });
        return new ArrayList<>(statMap.values());
    }

    @Transactional
    public void createCategory(Category category) {
        categoryMapper.insert(category);
        redisCache.delete(CATEGORY_CACHE_KEY);
    }

    @Transactional
    public void updateCategory(Long id, Category category) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BizException(404, "分类不存在");
        }
        category.setId(id);
        categoryMapper.updateById(category);
        redisCache.delete(CATEGORY_CACHE_KEY);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        if (productCount != null && productCount > 0) {
            throw new BizException("该分类下还有商品，不能删除");
        }
        categoryMapper.deleteById(id);
        redisCache.delete(CATEGORY_CACHE_KEY);
    }

    @Transactional
    public void createProduct(ProductForm form) {
        Product product = new Product();
        applyForm(product, form);
        product.setSales(0);
        productMapper.insert(product);
        saveSkus(product.getId(), form.getSkus(), product.getMainImage());
        redisCache.delete(HOT_CACHE_KEY);
    }

    @Transactional
    public void updateProduct(Long id, ProductForm form) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        applyForm(product, form);
        productMapper.updateById(product);
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        saveSkus(id, form.getSkus(), product.getMainImage());
        invalidateProductCache(id);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        invalidateProductCache(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
        invalidateProductCache(id);
    }

    private void applyForm(Product product, ProductForm form) {
        product.setCategoryId(form.getCategoryId());
        product.setName(form.getName());
        product.setSubtitle(form.getSubtitle());
        product.setBrand(form.getBrand());
        product.setRegion(form.getRegion());
        product.setMainImage(form.getMainImage());
        product.setDetail(form.getDetail());
        product.setPublishDate(form.getPublishDate() == null ? LocalDateTime.now() : form.getPublishDate());
        product.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    private void saveSkus(Long productId, List<ProductForm.SkuForm> skuForms, String defaultImage) {
        if (skuForms == null) {
            return;
        }
        skuForms.forEach(form -> {
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSpecName(form.getSpecName());
            sku.setSpecValue(form.getSpecValue());
            sku.setPrice(form.getPrice());
            sku.setStock(form.getStock() == null ? 0 : form.getStock());
            sku.setImage(form.getImage() == null || form.getImage().isBlank() ? defaultImage : form.getImage());
            sku.setStatus(form.getStatus() == null ? 1 : form.getStatus());
            skuMapper.insert(sku);
        });
    }

    private void invalidateProductCache(Long productId) {
        redisCache.delete(DETAIL_CACHE_PREFIX + productId);
        redisCache.delete(HOT_CACHE_KEY);
    }

    private String fetchNickname(Long userId) {
        try {
            Result<UserDTO> result = authClient.getUser(userId);
            if (result.isSuccess() && result.getData() != null) {
                String nickname = result.getData().getNickname();
                if (nickname != null && !nickname.isBlank()) {
                    return nickname;
                }
                return result.getData().getUsername();
            }
        } catch (Exception ignored) {
            // fallback
        }
        return "用户" + userId;
    }

    private List<ProductVO> enrichProducts(List<Product> products, boolean includeAllSkus) {
        if (products.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        List<ProductSku> skus = skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .in(ProductSku::getProductId, productIds)
                .eq(!includeAllSkus, ProductSku::getStatus, 1));
        Map<Long, List<ProductSku>> skuMap = skus.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));
        List<Long> categoryIds = products.stream().map(Product::getCategoryId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        return products.stream().map(product -> {
            ProductVO vo = new ProductVO();
            vo.setId(product.getId());
            vo.setCategoryId(product.getCategoryId());
            Category category = categoryMap.get(product.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
            vo.setName(product.getName());
            vo.setSubtitle(product.getSubtitle());
            vo.setBrand(product.getBrand());
            vo.setRegion(product.getRegion());
            vo.setMainImage(product.getMainImage());
            vo.setDetail(product.getDetail());
            vo.setPublishDate(product.getPublishDate());
            vo.setStatus(product.getStatus());
            vo.setSales(product.getSales());
            vo.setRating(product.getRating());
            List<ProductSku> productSkus = skuMap.getOrDefault(product.getId(), new ArrayList<>());
            vo.setSkus(productSkus);
            vo.setMinPrice(productSkus.stream().map(ProductSku::getPrice)
                    .filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null));
            vo.setTotalStock(productSkus.stream().mapToInt(s -> s.getStock() == null ? 0 : s.getStock()).sum());
            return vo;
        }).collect(Collectors.toList());
    }
}
