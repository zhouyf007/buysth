package com.shop.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shipment_track")
public class ShipmentTrack extends BaseEntity {

    private Long shipmentId;
    private String trackingNo;
    private String status;
    private String description;
    private LocalDateTime trackTime;
}

