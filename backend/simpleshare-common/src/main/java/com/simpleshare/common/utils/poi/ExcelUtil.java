package com.simpleshare.common.utils.poi;

import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.common.utils.Convert;
import com.simpleshare.common.utils.DateUtils;
import com.simpleshare.common.utils.ReflectUtils;
import com.simpleshare.common.config.SimpleShareConfig;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.UUID;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Excel相关处理
 *
 * @author SimpleShare
 */
public class ExcelUtil<T> {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static final String FORMULA_REGEX_STR = "=|-|\\+|@";

    public static final String[] FORMULA_STR = {"+", "-", "=", "@"};

    /**
     * Excel sheet最大行数，默认65536
     */
    public static final int sheetSize = 65536;

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    private Type type;

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    /**
     * 导入导出数据列表
     */
    private List<T> list;

    /**
     * 注解列表
     */
    private List<Object[]> fields;

    /**
     * 当前行号
     */
    private int rownum;

    /**
     * 标题
     */
    private String title;

    /**
     * 最大高度
     */
    private short maxHeight;

    /**
     * 统计列表
     */
    private Map<Integer, Double> statistics = new HashMap<Integer, Double>();

    /**
     * 数字格式
     */
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

    /**
     * 实体对象
     */
    public Class<T> clazz;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void init(List<T> list, String sheetName, String title, Type type) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        this.list = list;
        this.sheetName = sheetName;
        this.type = type;
        this.title = title;
        createExcelField();
        createWorkbook();
        createTitle();
        createSubHead();
    }

    /**
     * 创建excel第一行标题
     */
    public void createTitle() {
        if (StringUtils.isNotEmpty(title)) {
            Row titleRow = sheet.createRow(rownum == 0 ? rownum++ : 0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), titleRow.getRowNum(),
                    this.fields.size() - 1));
        }
    }

    /**
     * 创建对象的子列表
     */
    public void createSubHead() {
        if (Type.EXPORT.equals(type)) {
            Row row = sheet.createRow(rownum);
            int column = 0;
            // 写入各个字段的列头名称
            for (Object[] os : fields) {
                Cell cell = row.createCell(column++);
                cell.setCellStyle(styles.get("header"));
                cell.setCellValue((String) os[1]);
            }
            if (Type.EXPORT.equals(type)) {
                fillExcelData(rownum + 1, rownum);
                addStatisticsRow();
            }
        }
    }

    /**
     * 填充excel数据
     *
     * @param index 序号
     * @param row   当前行
     */
    public void fillExcelData(int index, int row) {
        int startNo = index;
        int endNo = Math.min(startNo + sheetSize - 1, startNo + list.size() - 1);
        for (int i = startNo; i < endNo; i++) {
            row = sheet.createRow(i).getRowNum();
            // 得到导出对象.
            T vo = (T) list.get(i);
            int column = 0;
            for (Object[] os : fields) {
                Field field = (Field) os[0];
                Cell cell = sheet.getRow(row).createCell(column++);
                cell.setCellStyle(styles.get("data"));
                // 设置行高
                sheet.getRow(row).setHeight(maxHeight);
                try {
                    // 如果数据存在就填入,不存在填入空格.
                    Class<?> fieldType = field.getType();
                    Object value = field.get(vo);
                    String dateFormat = field.getAnnotation(ExcelProperty.class) != null ? field.getAnnotation(ExcelProperty.class).dateFormat() : "";
                    String readConverterExp = field.getAnnotation(ExcelProperty.class) != null ? field.getAnnotation(ExcelProperty.class).readConverterExp() : "";
                    String separator = field.getAnnotation(ExcelProperty.class) != null ? field.getAnnotation(ExcelProperty.class).separator() : ",";
                    if (StringUtils.isNotEmpty(dateFormat) && StringUtils.isNotNull(value)) {
                        cell.setCellValue(parseDateToStr(dateFormat, value));
                    } else if (StringUtils.isNotEmpty(readConverterExp) && StringUtils.isNotNull(value)) {
                        cell.setCellValue(convertByExp(Convert.toStr(value), readConverterExp, separator));
                    } else if (value instanceof BigDecimal && -1 != ((BigDecimal) value).compareTo(BigDecimal.ZERO)) {
                        cell.setCellValue(((BigDecimal) value).toString());
                    } else if (!fieldType.isPrimitive() && value != null) {
                        cell.setCellValue(Convert.toStr(value));
                    } else {
                        cell.setCellValue("");
                    }
                    addStatisticsData(column, Convert.toStr(value), field);
                } catch (Exception e) {
                    log.error("导出Excel失败{}", e);
                }
            }
        }
    }

    /**
     * 创建表格样式
     *
     * @param wb 工作薄对象
     * @return 样式列表
     */
    private Map<String, CellStyle> createStyles(Workbook wb) {
        // 写入各条记录,每条记录对应excel表中的一行
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        style.setFont(titleFont);
        styles.put("title", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("data", style);

        style = wb.createCellStyle();
        style.cloneStyleFrom(styles.get("data"));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = wb.createFont();
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(headerFont);
        styles.put("header", style);

        return styles;
    }

    /**
     * 创建工作簿
     */
    public void createWorkbook() {
        this.wb = new XSSFWorkbook();
        this.sheet = wb.createSheet();
        wb.setSheetName(0, sheetName);
        this.styles = createStyles(wb);
    }

    /**
     * 创建excel字段
     */
    public void createExcelField() {
        this.fields = getFields();
        this.fields = this.fields.stream().sorted(Comparator.comparing(objects -> ((ExcelProperty) objects[1]).sort())).collect(Collectors.toList());
        this.maxHeight = getRowHeight();
    }

    /**
     * 得到所有定义字段
     */
    private List<Object[]> getFields() {
        List<Object[]> fields = new ArrayList<Object[]>();
        List<Field> tempFields = new ArrayList<>();
        tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        for (Field field : tempFields) {
            // 单注解
            if (field.isAnnotationPresent(ExcelProperty.class)) {
                ExcelProperty attr = field.getAnnotation(ExcelProperty.class);
                if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
                    field.setAccessible(true);
                    fields.add(new Object[]{field, attr, attr.type()});
                }
            }
        }
        return fields;
    }

    /**
     * 根据注解获取最大行高
     */
    public short getRowHeight() {
        double maxHeight = 0;
        for (Object[] os : this.fields) {
            ExcelProperty excel = (ExcelProperty) os[1];
            maxHeight = Math.max(maxHeight, excel.height());
        }
        return (short) (maxHeight * 20);
    }

    /**
     * 创建统计行
     */
    public void addStatisticsRow() {
        if (statistics.size() > 0) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Set<Integer> keys = statistics.keySet();
            Cell cell = row.createCell(0);
            cell.setCellStyle(styles.get("data"));
            cell.setCellValue("合计");

            for (Integer key : keys) {
                cell = row.createCell(key);
                cell.setCellStyle(styles.get("data"));
                cell.setCellValue(DOUBLE_FORMAT.format(statistics.get(key)));
            }
            statistics.clear();
        }
    }

    /**
     * 添加到统计行
     *
     * @param index 字段序号
     * @param text  字段值
     * @param field 字段对象
     */
    private void addStatisticsData(Integer index, String text, Field field) {
        if (field != null && field.isAnnotationPresent(ExcelProperty.class)) {
            ExcelProperty excel = field.getAnnotation(ExcelProperty.class);
            if (excel.isStatistics()) {
                Double temp = 0D;
                if (!statistics.containsKey(index)) {
                    statistics.put(index, temp);
                }
                try {
                    temp = Double.valueOf(text);
                } catch (NumberFormatException e) {
                }
                statistics.put(index, statistics.get(index) + temp);
            }
        }
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     *
     * @param propertyValue 参数值
     * @param converterExp  翻译注解
     * @param separator     分隔符
     * @return 解析后值
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(",");
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(separator, propertyValue)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0] + separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 日期或者时间格式化
     *
     * @param dateFormat 日期格式
     * @param val        被格式化的数据
     * @return 格式化后的数据
     */
    public String parseDateToStr(String dateFormat, Object val) {
        if (val == null) {
            return "";
        }
        String str;
        if (val instanceof Date) {
            str = DateUtils.parseDateToStr(dateFormat, (Date) val);
        } else if (val instanceof LocalDateTime) {
            str = DateUtils.parseDateToStr(dateFormat, DateUtils.toDate((LocalDateTime) val));
        } else if (val instanceof LocalDate) {
            str = DateUtils.parseDateToStr(dateFormat, DateUtils.toDate((LocalDate) val));
        } else {
            str = val.toString();
        }
        return str;
    }

    /**
     * 是否有对象的子列表
     */
    public boolean isSubList() {
        return false;
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param response      返回数据
     * @param list          导出数据集合
     * @param sheetName     工作表的名称
     * @return 结果
     */
    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName) {
        exportExcel(response, list, sheetName, StringUtils.EMPTY);
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param response      返回数据
     * @param list          导出数据集合
     * @param sheetName     工作表的名称
     * @param title         标题
     * @return 结果
     */
    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName, String title) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        this.init(list, sheetName, title, Type.EXPORT);
        exportExcel(response);
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param response 返回数据
     */
    public void exportExcel(HttpServletResponse response) {
        try {
            writeStream(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出Excel异常{}", e.getMessage());
        }
    }

    /**
     * 将数据写入到输出流
     *
     * @param out 输出流
     */
    public void writeStream(OutputStream out) {
        try {
            wb.write(out);
        } catch (IOException e) {
            log.error("导出Excel异常{}", e.getMessage());
        } finally {
            if (wb != null) {
                try {
                    wb.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 导入Excel
     *
     * @param is 输入流
     * @return 结果
     */
    public List<T> importExcel(InputStream is) {
        return importExcel(is, 1, 1);
    }

    /**
     * 导入Excel
     *
     * @param is       输入流
     * @param titleNum 标题占用行数
     * @param headerNum 表头占用行数
     * @return 结果
     */
    public List<T> importExcel(InputStream is, int titleNum, int headerNum) {
        this.type = Type.IMPORT;
        this.wb = null;
        List<T> list = new ArrayList<T>();
        // 如果为Null,则说明文件不存在
        if (StringUtils.isNull(is)) {
            throw new RuntimeException("导入文件为空");
        }
        try {
            this.wb = WorkbookFactory.create(is);
            Sheet sheet = wb.getSheetAt(0);
            // 获取最大行数
            int rows = sheet.getLastRowNum();

            if (rows > 0) {
                // 定义实体类字段
                Map<String, Integer> cellMap = new HashMap<String, Integer>();
                if (rows >= titleNum + headerNum) {
                    Row heard = sheet.getRow(titleNum);
                    for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
                        Cell cell = heard.getCell(i);
                        if (StringUtils.isNotNull(cell)) {
                            String value = this.getCellValue(heard, i).toString();
                            cellMap.put(value, i);
                        } else {
                            cellMap.put(null, i);
                        }
                    }
                    // 有数据时才处理 得到类的所有field.
                    this.fields = this.getFields();
                    Map<Integer, Object[]> fieldsMap = new HashMap<Integer, Object[]>();
                    for (Object[] objects : this.fields) {
                        ExcelProperty attr = (ExcelProperty) objects[1];
                        Integer column = cellMap.get(attr.name());
                        if (column != null) {
                            fieldsMap.put(column, objects);
                        }
                    }
                    for (int i = titleNum + headerNum; i <= rows; i++) {
                        // 从第2行开始取数据,默认第一行是表头.
                        Row row = sheet.getRow(i);
                        // 判断当前行是否是空行
                        if (isRowEmpty(row)) {
                            continue;
                        }
                        T entity = null;
                        try {
                            entity = (T) clazz.newInstance();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            throw new RuntimeException(e.getMessage());
                        }
                        for (Map.Entry<Integer, Object[]> entry : fieldsMap.entrySet()) {
                            Object val = this.getCellValue(row, entry.getKey());

                            // 如果不存在实例则新建.
                            entity = (entity == null ? (T) clazz.newInstance() : entity);
                            // 从map中得到对应列的field.
                            Field field = (Field) entry.getValue()[0];
                            ExcelProperty attr = (ExcelProperty) entry.getValue()[1];
                            // 取得类型,并根据对象类型设置值.
                            Class<?> fieldType = field.getType();
                            if (String.class == fieldType) {
                                String s = Convert.toStr(val);
                                if (StringUtils.endsWith(s, ".0")) {
                                    val = StringUtils.substringBefore(s, ".0");
                                } else {
                                    String dateFormat = field.getAnnotation(ExcelProperty.class).dateFormat();
                                    if (StringUtils.isNotEmpty(dateFormat)) {
                                        val = parseDateToStr(dateFormat, val);
                                    } else {
                                        String readConverterExp = field.getAnnotation(ExcelProperty.class).readConverterExp();
                                        if (StringUtils.isNotEmpty(readConverterExp)) {
                                            val = reverseByExp(Convert.toStr(val), readConverterExp, attr.separator());
                                        } else if (StringUtils.isNotEmpty(attr.dictType())) {
                                            val = reverseDictByExp(Convert.toStr(val), attr.dictType(), attr.separator());
                                        }
                                    }
                                }
                            } else if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StringUtils.isNumeric(Convert.toStr(val))) {
                                val = Convert.toInt(val);
                            } else if ((Long.TYPE == fieldType || Long.class == fieldType) && StringUtils.isNumeric(Convert.toStr(val))) {
                                val = Convert.toLong(val);
                            } else if (Double.TYPE == fieldType || Double.class == fieldType) {
                                val = Convert.toDouble(val);
                            } else if (Float.TYPE == fieldType || Float.class == fieldType) {
                                val = Convert.toFloat(val);
                            } else if (BigDecimal.class == fieldType) {
                                val = Convert.toBigDecimal(val);
                            } else if (Date.class == fieldType) {
                                if (val instanceof String) {
                                    val = DateUtils.parseDate(val);
                                } else if (val instanceof Double) {
                                    val = DateUtil.getJavaDate((Double) val);
                                }
                            } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
                                val = Convert.toBool(val, false);
                            }
                            if (StringUtils.isNotNull(fieldType)) {
                                String propertyName = field.getName();
                                if (StringUtils.isNotEmpty(attr.targetAttr())) {
                                    propertyName = field.getName() + "." + attr.targetAttr();
                                } else if (StringUtils.isNotEmpty(attr.readConverterExp())) {
                                    val = reverseByExp(Convert.toStr(val), attr.readConverterExp(), attr.separator());
                                } else if (StringUtils.isNotEmpty(attr.dictType())) {
                                    val = reverseDictByExp(Convert.toStr(val), attr.dictType(), attr.separator());
                                }
                                ReflectUtils.invokeSetter(entity, propertyName, val);
                            }
                        }
                        list.add(entity);
                    }
                }
            }
        } catch (Exception e) {
            String msg = "导入Excel失败，请检查文件格式是否正确。" + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg);
        } finally {
            if (wb != null) {
                try {
                    wb.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @return 结果
     */
    public void importTemplateExcel(HttpServletResponse response, String sheetName) {
        importTemplateExcel(response, sheetName, StringUtils.EMPTY);
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @return 结果
     */
    public void importTemplateExcel(HttpServletResponse response, String sheetName, String title) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        this.init(null, sheetName, title, Type.IMPORT);
        importTemplateExcel(response);
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @return 结果
     */
    public void importTemplateExcel(HttpServletResponse response) {
        try {
            writeStream(response.getOutputStream());
        } catch (IOException e) {
            log.error("导入模板异常{}", e.getMessage());
        }
    }

    /**
     * 获取单元格值
     *
     * @param row    获取的行
     * @param column 获取单元格列号
     * @return 单元格值
     */
    public Object getCellValue(Row row, int column) {
        if (row == null) {
            return row;
        }
        Object val = "";
        try {
            Cell cell = row.getCell(column);
            if (StringUtils.isNotNull(cell)) {
                if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                    val = cell.getNumericCellValue();
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
                    } else {
                        if ((Double) val % 1 != 0) {
                            val = new BigDecimal(val.toString());
                        } else {
                            val = new DecimalFormat("0").format(val);
                        }
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cell.getCellType() == CellType.ERROR) {
                    val = cell.getErrorCellValue();
                }

            }
        } catch (Exception e) {
            return val;
        }
        return val;
    }

    /**
     * 判断是否是空行
     *
     * @param row 判断的行
     * @return
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }



    /**
     * 反向解析值字典值 男=0,女=1,未知=2
     *
     * @param dictValue 字典值
     * @param dictType  字典类型
     * @param separator 分隔符
     * @return 字典标签
     */
    public static String reverseDictByExp(String dictValue, String dictType, String separator) {
        return StringUtils.EMPTY;
    }

    /**
     * 解析字典值
     *
     * @param dictValue 字典值
     * @param dictType  字典类型
     * @param separator 分隔符
     * @return 字典标签
     */
    public static String convertDictByExp(String dictValue, String dictType, String separator) {
        return StringUtils.EMPTY;
    }

    /**
     * 数据处理器
     *
     * @param value 数据值
     * @param excel 数据注解
     * @return
     */
    public String dataFormatHandlerAdapter(Object value, ExcelProperty excel) {
        try {
            Object instance = excel.handler().newInstance();
            Method formatMethod = excel.handler().getMethod("format", new Class[]{Object.class, String[].class});
            value = formatMethod.invoke(instance, value, excel.args());
        } catch (Exception e) {
            log.error("不能格式化数据 " + excel.handler(), e.getMessage());
        }
        return Convert.toStr(value);
    }

    /**
     * 合计统计信息
     */
    private void addStatisticsData(Integer index, String text, ExcelProperty entity) {
        if (entity != null && entity.isStatistics()) {
            Double temp = 0D;
            if (!statistics.containsKey(index)) {
                statistics.put(index, temp);
            }
            try {
                temp = Double.valueOf(text);
            } catch (NumberFormatException e) {
            }
            statistics.put(index, statistics.get(index) + temp);
        }
    }

    /**
     * 编码文件名
     */
    public String encodingFilename(String filename) {
        filename = UUID.randomUUID().toString() + "_" + filename + ".xlsx";
        return filename;
    }

    /**
     * 获取下载路径
     *
     * @param filename 文件名称
     */
    public String getAbsoluteFile(String filename) {
        String downloadPath = SimpleShareConfig.getDownloadPath() + filename;
        File desc = new File(downloadPath);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        return downloadPath;
    }

    /**
     * 获取bean中的属性值
     *
     * @param vo    实体对象
     * @param field 字段
     * @param excel 注解
     * @return 最终的属性值
     * @throws Exception
     */
    private Object getTargetValue(T vo, Field field, ExcelProperty excel) throws Exception {
        Object o = field.get(vo);
        if (StringUtils.isNotEmpty(excel.targetAttr())) {
            String target = excel.targetAttr();
            if (target.indexOf(".") > -1) {
                String[] targets = target.split("\\.");
                for (String name : targets) {
                    o = getValue(o, name);
                }
            } else {
                o = getValue(o, target);
            }
        }
        return o;
    }

    /**
     * 以类的属性的get方法方法形式获取值
     *
     * @param o
     * @param name
     * @return value
     * @throws Exception
     */
    private Object getValue(Object o, String name) throws Exception {
        if (StringUtils.isNotNull(o) && StringUtils.isNotEmpty(name)) {
            Class<?> clazz = o.getClass();
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            o = field.get(o);
        }
        return o;
    }



    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    public enum Type {
        EXPORT(0), IMPORT(1), ALL(2);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}