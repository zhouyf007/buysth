package com.shop.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notify_message")
public class NotifyMessage extends BaseEntity {

    private Long userId;
    private String orderNo;
    private String type;
    private String title;
    private String content;
    private Integer readStatus;
    private Integer userDeleted;
    private Integer adminDeleted;
}
