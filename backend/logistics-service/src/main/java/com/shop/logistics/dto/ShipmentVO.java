package com.shop.logistics.dto;

import com.shop.logistics.entity.ShipmentTrack;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipmentVO {

    private Long id;
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
    private LocalDateTime createTime;
    private List<ShipmentTrack> tracks;
}

