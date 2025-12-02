package com.simpleshare.framework.tenant;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.simpleshare.common.utils.StringUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

/**
 * 租户拦截器
 * 自动在SQL中添加租户条件
 *
 * @author SimpleShare
 */
public class TenantInterceptor implements InnerInterceptor {
    
    private static final String TENANT_ID_COLUMN = "tenant_id";
    
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 检查是否忽略租户隔离
        if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) {
            return;
        }
        
        // 获取当前租户ID
        String tenantId = TenantContextHolder.getTenantId();
        if (StringUtils.isEmpty(tenantId)) {
            return;
        }
        
        PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
        mpBoundSql.sql(this.parserSingle(mpBoundSql.sql(), tenantId));
    }
    
    /**
     * 解析SQL并添加租户条件
     *
     * @param sql      原始SQL
     * @param tenantId 租户ID
     * @return 处理后的SQL
     */
    protected String parserSingle(String sql, String tenantId) {
        try {
            Select select = (Select) net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sql);
            this.processSelect(select.getSelectBody(), tenantId);
            return select.toString();
        } catch (Exception e) {
            // 解析失败时返回原SQL
            return sql;
        }
    }
    
    /**
     * 处理Select语句
     *
     * @param selectBody Select主体
     * @param tenantId   租户ID
     */
    protected void processSelect(SelectBody selectBody, String tenantId) {
        if (selectBody instanceof PlainSelect) {
            this.processPlainSelect((PlainSelect) selectBody, tenantId);
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selectBodyList = setOperationList.getSelects();
            selectBodyList.forEach(s -> this.processSelect(s, tenantId));
        }
    }
    
    /**
     * 处理PlainSelect
     *
     * @param plainSelect PlainSelect对象
     * @param tenantId    租户ID
     */
    protected void processPlainSelect(PlainSelect plainSelect, String tenantId) {
        Expression tenantCondition = this.buildTenantCondition(tenantId);
        Expression where = plainSelect.getWhere();
        if (where == null) {
            plainSelect.setWhere(tenantCondition);
        } else {
            plainSelect.setWhere(new AndExpression(where, tenantCondition));
        }
    }
    
    /**
     * 构建租户条件
     *
     * @param tenantId 租户ID
     * @return 租户条件表达式
     */
    protected Expression buildTenantCondition(String tenantId) {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(TENANT_ID_COLUMN));
        equalsTo.setRightExpression(new LongValue(tenantId));
        return equalsTo;
    }
}