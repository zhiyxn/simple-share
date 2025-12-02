package com.simpleshare.system.service;

import com.simpleshare.system.domain.AiTechCardClick;
import com.simpleshare.system.dto.AiTechCardClickRequest;

import java.util.List;

/**
 * AiTech 卡片点击统计服务
 */
public interface IAiTechCardClickService {

    /**
     * 记录一次卡片点击（不存在则创建）
     */
    AiTechCardClick recordClick(AiTechCardClickRequest request);

    /**
     * 查询当前租户下全部卡片统计
     */
    List<AiTechCardClick> listCardClicks(Long tenantId);

    /**
     * 查询点击数前 N 的卡片
     */
    List<AiTechCardClick> listTopCardClicks(Long tenantId, int limit);
}
