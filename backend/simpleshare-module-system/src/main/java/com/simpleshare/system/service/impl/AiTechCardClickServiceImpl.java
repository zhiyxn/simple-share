package com.simpleshare.system.service.impl;

import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.tenant.TenantContextHolder;
import com.simpleshare.system.domain.AiTechCardClick;
import com.simpleshare.system.dto.AiTechCardClickRequest;
import com.simpleshare.system.mapper.AiTechCardClickMapper;
import com.simpleshare.system.service.IAiTechCardClickService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * AiTech 卡片点击统计服务实现
 */
@Service
public class AiTechCardClickServiceImpl implements IAiTechCardClickService {

    private final AiTechCardClickMapper cardClickMapper;

    public AiTechCardClickServiceImpl(AiTechCardClickMapper cardClickMapper) {
        this.cardClickMapper = cardClickMapper;
    }

    @Override
    public AiTechCardClick recordClick(AiTechCardClickRequest request) {
        if (request == null) {
            return null;
        }

        Long tenantId = resolveTenantId(null);
        LocalDateTime now = LocalDateTime.now();
        AiTechCardClick existing = cardClickMapper.selectByTenantAndKey(tenantId, request.getCardKey());
        if (existing == null) {
            AiTechCardClick entity = new AiTechCardClick();
            entity.setTenantId(tenantId);
            entity.setCardKey(request.getCardKey());
            entity.setCardTitle(request.getCardTitle());
            entity.setCardPath(request.getCardPath());
            entity.setCardCategory(request.getCardCategory());
            entity.setClickCount(1L);
            entity.setLastClickTime(now);
            cardClickMapper.insert(entity);
            return entity;
        }

        cardClickMapper.incrementClickCount(existing.getId(),
                request.getCardTitle(),
                request.getCardPath(),
                request.getCardCategory(),
                now);
        existing.setCardTitle(request.getCardTitle());
        existing.setCardPath(request.getCardPath());
        existing.setCardCategory(request.getCardCategory());
        existing.setClickCount((existing.getClickCount() == null ? 0L : existing.getClickCount()) + 1);
        existing.setLastClickTime(now);
        return existing;
    }

    @Override
    public List<AiTechCardClick> listCardClicks(Long tenantId) {
        Long resolvedTenantId = resolveTenantId(tenantId);
        if (resolvedTenantId == null) {
            return Collections.emptyList();
        }
        return cardClickMapper.selectByTenant(resolvedTenantId);
    }

    @Override
    public List<AiTechCardClick> listTopCardClicks(Long tenantId, int limit) {
        Long resolvedTenantId = resolveTenantId(tenantId);
        if (resolvedTenantId == null) {
            return Collections.emptyList();
        }
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        return cardClickMapper.selectTopByTenant(resolvedTenantId, safeLimit);
    }

    private Long resolveTenantId(Long explicitTenantId) {
        if (explicitTenantId != null) {
            return explicitTenantId;
        }
        String contextId = TenantContextHolder.getTenantId();
        if (StringUtils.isNotBlank(contextId)) {
            try {
                return Long.valueOf(contextId);
            } catch (NumberFormatException ignored) {
                return 1L;
            }
        }
        return 1L;
    }
}
