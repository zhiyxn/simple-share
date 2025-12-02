package com.simpleshare.infra.controller.admin;

// import com.simpleshare.common.annotation.Log;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.enums.BusinessType;
import com.simpleshare.common.utils.poi.ExcelUtil;
import com.simpleshare.infra.domain.InfraFileConfig;
import com.simpleshare.infra.service.IInfraFileConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 文件配置Controller
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/infra/file-config")
public class InfraFileConfigController extends BaseController {

    @Autowired
    private IInfraFileConfigService infraFileConfigService;

    /**
     * 查询文件配置列表
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(InfraFileConfig infraFileConfig) {
        return infraFileConfigService.selectInfraFileConfigListPage(infraFileConfig);
    }

    /**
     * 导出文件配置列表
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:export')")
    // @Log(title = "文件配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InfraFileConfig infraFileConfig) {
        List<InfraFileConfig> list = infraFileConfigService.selectInfraFileConfigList(infraFileConfig);
        ExcelUtil<InfraFileConfig> util = new ExcelUtil<InfraFileConfig>(InfraFileConfig.class);
        util.exportExcel(response, list, "文件配置数据");
    }

    /**
     * 获取文件配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(infraFileConfigService.selectInfraFileConfigById(id));
    }

    /**
     * 新增文件配置
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:add')")
    // @Log(title = "文件配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InfraFileConfig infraFileConfig) {
        return toAjax(infraFileConfigService.insertInfraFileConfig(infraFileConfig));
    }

    /**
     * 修改文件配置
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:edit')")
    // @Log(title = "文件配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InfraFileConfig infraFileConfig) {
        return toAjax(infraFileConfigService.updateInfraFileConfig(infraFileConfig));
    }

    /**
     * 删除文件配置
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:remove')")
    // @Log(title = "文件配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(infraFileConfigService.deleteInfraFileConfigByIds(ids));
    }

    /**
     * 获取主配置
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:query')")
    @GetMapping("/master")
    public AjaxResult getMasterConfig() {
        InfraFileConfig config = infraFileConfigService.getMasterConfig();
        if (config != null && config.getConfig() != null) {
            try {
                // 创建配置的副本，避免修改原始数据
                InfraFileConfig filteredConfig = new InfraFileConfig();
                filteredConfig.setId(config.getId());
                filteredConfig.setName(config.getName());
                filteredConfig.setStorage(config.getStorage());
                filteredConfig.setMaster(config.getMaster());
                filteredConfig.setRemark(config.getRemark());
                filteredConfig.setCreateTime(config.getCreateTime());
                filteredConfig.setUpdateTime(config.getUpdateTime());
                
                // 解析JSON配置
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> configMap = objectMapper.readValue(
                        config.getConfig(), new TypeReference<Map<String, Object>>() {});
                
                // 过滤敏感信息，只保留公共配置
                Map<String, Object> filteredConfigMap = new HashMap<>();
                if (configMap.containsKey("domain")) {
                    filteredConfigMap.put("domain", configMap.get("domain"));
                    // 将domain单独提取到顶层，便于前端直接获取
                    filteredConfig.setDomain((String) configMap.get("domain"));
                }
                if (configMap.containsKey("endpoint")) {
                    filteredConfigMap.put("endpoint", configMap.get("endpoint"));
                }
                if (configMap.containsKey("bucket")) {
                    filteredConfigMap.put("bucket", configMap.get("bucket"));
                }
                
                // 将过滤后的配置转换回JSON字符串
                filteredConfig.setConfig(objectMapper.writeValueAsString(filteredConfigMap));
                
                return success(filteredConfig);
            } catch (Exception e) {
                // 解析失败时返回原始配置（不含敏感信息）
                InfraFileConfig safeConfig = new InfraFileConfig();
                safeConfig.setId(config.getId());
                safeConfig.setName(config.getName());
                safeConfig.setStorage(config.getStorage());
                safeConfig.setMaster(config.getMaster());
                safeConfig.setRemark(config.getRemark());
                safeConfig.setCreateTime(config.getCreateTime());
                safeConfig.setUpdateTime(config.getUpdateTime());
                safeConfig.setConfig("{}"); // 空配置对象
                return success(safeConfig);
            }
        }
        return success(config);
    }

    /**
     * 设置主配置
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:edit')")
    // @Log(title = "设置主配置", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/master")
    public AjaxResult setMasterConfig(@PathVariable Long id) {
        return toAjax(infraFileConfigService.setMasterConfig(id));
    }

    /**
     * 测试配置连接
     */
    @PreAuthorize("@ss.hasPermi('infra:fileConfig:query')")
    @PostMapping("/{id}/test")
    public AjaxResult testConfig(@PathVariable Long id) {
        boolean result = infraFileConfigService.testConfig(id);
        if (result) {
            return success("配置测试成功");
        } else {
            return error("配置测试失败");
        }
    }
}