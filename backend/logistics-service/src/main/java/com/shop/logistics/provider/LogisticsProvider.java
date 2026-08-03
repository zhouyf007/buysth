package com.shop.logistics.provider;

public interface LogisticsProvider {

    String companyCode();

    String companyName();

    String createTrackingNo(String shipmentNo);
}

