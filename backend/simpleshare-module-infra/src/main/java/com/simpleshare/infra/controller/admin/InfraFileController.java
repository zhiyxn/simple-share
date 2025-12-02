package com.simpleshare.infra.controller.admin;

// import com.simpleshare.common.annotation.Log;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.enums.BusinessType;
import com.simpleshare.common.utils.poi.ExcelUtil;
import com.simpleshare.infra.domain.InfraFile;
import com.simpleshare.infra.service.IInfraFileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件信息Controller
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/infra/file")
public class InfraFileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(InfraFileController.class);
    
    @Autowired
    private IInfraFileService infraFileService;
    


    /**
     * 查询文件信息列表
     */
    @PreAuthorize("@ss.hasPermi('infra:file:list')")
    @GetMapping("/list")
    public TableDataInfo list(InfraFile infraFile) {
        return infraFileService.selectInfraFileListPage(infraFile);
    }

    /**
     * 导出文件信息列表
     */
    @PreAuthorize("@ss.hasPermi('infra:file:export')")
    // @Log(title = "文件信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InfraFile infraFile) {
        List<InfraFile> list = infraFileService.selectInfraFileList(infraFile);
        ExcelUtil<InfraFile> util = new ExcelUtil<InfraFile>(InfraFile.class);
        util.exportExcel(response, list, "文件信息数据");
    }

    /**
     * 获取文件信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('infra:file:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(infraFileService.selectInfraFileById(id));
    }

    /**
     * 新增文件信息
     */
    @PreAuthorize("@ss.hasPermi('infra:file:add')")
    // @Log(title = "文件信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InfraFile infraFile) {
        return toAjax(infraFileService.insertInfraFile(infraFile));
    }

    /**
     * 修改文件信息
     */
    @PreAuthorize("@ss.hasPermi('infra:file:edit')")
    // @Log(title = "文件信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InfraFile infraFile) {
        return toAjax(infraFileService.updateInfraFile(infraFile));
    }

    /**
     * 删除文件信息
     */
    @PreAuthorize("@ss.hasPermi('infra:file:remove')")
    // @Log(title = "文件信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(infraFileService.deleteInfraFileByIds(ids));
    }

    /**
     * 上传文件
     */
    @PreAuthorize("@ss.hasPermi('infra:file:upload')")
    // @Log(title = "文件上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) {
        try {
            InfraFile infraFile = infraFileService.uploadFile(file);
            return success(infraFile);
        } catch (Exception e) {
            return error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件统计信息
     */
    @PreAuthorize("@ss.hasPermi('infra:file:query')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        return success(infraFileService.getFileStatistics());
    }

    /**
     * 根据路径删除文件
     */
    @PreAuthorize("@ss.hasPermi('infra:file:remove')")
    // @Log(title = "删除文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/path")
    public AjaxResult deleteByPath(@RequestParam String path) {
        return toAjax(infraFileService.deleteFileByPath(path));
    }

    /**
     * 简单文件上传接口（无权限控制）
     */
    @PostMapping("/simple-upload")
    public AjaxResult simpleUpload(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = infraFileService.uploadSingleFile(file);
            return AjaxResult.success("文件上传成功", fileUrl);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PreAuthorize("@ss.hasPermi('infra:file:upload')")
    @PostMapping("/batch-upload")
    public AjaxResult batchUpload(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> fileUrls = infraFileService.uploadMultipleFiles(files);
            return AjaxResult.success("文件批量上传成功", fileUrls);
        } catch (Exception e) {
            logger.error("文件批量上传失败", e);
            return AjaxResult.error("文件批量上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据URL删除文件
     */
    @PreAuthorize("@ss.hasPermi('infra:file:remove')")
    @DeleteMapping("/url")
    public AjaxResult deleteByUrl(@RequestParam("fileUrl") String fileUrl) {
        try {
            infraFileService.deleteFileByUrl(fileUrl);
            return AjaxResult.success("文件删除成功");
        } catch (Exception e) {
            logger.error("文件删除失败", e);
            return AjaxResult.error("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 预览文件
     */
    @PreAuthorize("@ss.hasPermi('infra:file:query')")
    @GetMapping("/preview/{id}")
    public void previewFile(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            // 获取文件信息
            InfraFile infraFile = infraFileService.selectInfraFileById(id);
            if (infraFile == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 获取文件内容
            byte[] content = infraFileService.getFileContent(infraFile.getPath());
            
            // 设置响应头
            response.setContentType(infraFile.getType());
            response.setContentLength(content.length);
            
            // 输出文件内容
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("文件预览失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件预览失败: " + e.getMessage());
            } catch (IOException ex) {
                logger.error("响应输出失败", ex);
            }
        }
    }
    
    /**
     * 下载文件
     */
    @PreAuthorize("@ss.hasPermi('infra:file:query')")
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            // 获取文件信息
            InfraFile infraFile = infraFileService.selectInfraFileById(id);
            if (infraFile == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 获取文件内容
            byte[] content = infraFileService.getFileContent(infraFile.getPath());
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setContentLength(content.length);
            
            // 对文件名进行URL编码，避免中文乱码
            String encodedFilename = URLEncoder.encode(infraFile.getName(), StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
            
            // 输出文件内容
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
        } catch (Exception e) {
            logger.error("文件下载失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("文件下载失败: " + e.getMessage());
            } catch (IOException ex) {
                logger.error("响应输出失败", ex);
            }
        }
    }
}