package com.shop.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipmentDTO {

    private Long id;
    private String shipmentNo;
    private String orderNo;
    private String companyCode;
    private String companyName;
    private String trackingNo;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime createTime;
    private List<TrackDTO> tracks;

    @Data
    public static class TrackDTO {
        private String status;
        private String description;
        private LocalDateTime trackTime;
    }
}

