package com.shop.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notify_log")
public class NotifyLog extends BaseEntity {

    private Long messageId;
    private String channel;
    private String target;
    private String status;
    private String error;
}

