package com.simpleshare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.system.domain.AiTechCardClick;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AiTech 卡片点击统计 Mapper
 */
@Mapper
public interface AiTechCardClickMapper extends BaseMapper<AiTechCardClick> {

    AiTechCardClick selectByTenantAndKey(@Param("tenantId") Long tenantId, @Param("cardKey") String cardKey);

    List<AiTechCardClick> selectByTenant(@Param("tenantId") Long tenantId);

    List<AiTechCardClick> selectTopByTenant(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    int incrementClickCount(@Param("id") Long id,
                            @Param("cardTitle") String cardTitle,
                            @Param("cardPath") String cardPath,
                            @Param("cardCategory") String cardCategory,
                            @Param("lastClickTime") LocalDateTime lastClickTime);
}
