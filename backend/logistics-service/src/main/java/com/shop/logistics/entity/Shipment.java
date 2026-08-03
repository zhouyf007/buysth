package com.shop.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shipment")
public class Shipment extends BaseEntity {

    private String shipmentNo;
    private String orderNo;
    private Long userId;
    private String companyCode;
    private String companyName;
    private String trackingNo;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
}

