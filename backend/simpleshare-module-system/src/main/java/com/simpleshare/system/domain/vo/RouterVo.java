package com.simpleshare.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 路由配置信息
 *
 * @author SimpleShare
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {
    /**
     * 路由名字
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 路由参数：如 {"id": 1, "name": "ry"}
     */
    private String query;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaVo meta;

    /**
     * 子路由
     */
    private List<RouterVo> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Boolean getAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(Boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    public MetaVo getMeta() {
        return meta;
    }

    public void setMeta(MetaVo meta) {
        this.meta = meta;
    }

    public List<RouterVo> getChildren() {
        return children;
    }

    public void setChildren(List<RouterVo> children) {
        this.children = children;
    }

    /**
     * 路由显示信息
     */
    public static class MetaVo {
        /**
         * 设置该路由在侧边栏和面包屑中展示的名字
         */
        private String title;

        /**
         * 设置该路由的图标，对应路径src/assets/icons/svg
         */
        private String icon;

        /**
         * 设置为true，则不会被 <keep-alive>缓存
         */
        private boolean noCache;

        /**
         * 内链地址（http(s)://开头）
         */
        private String link;

        public MetaVo() {
        }

        public MetaVo(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }

        public MetaVo(String title, String icon, boolean noCache) {
            this.title = title;
            this.icon = icon;
            this.noCache = noCache;
        }

        public MetaVo(String title, String icon, boolean noCache, String link) {
            this.title = title;
            this.icon = icon;
            this.noCache = noCache;
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public boolean isNoCache() {
            return noCache;
        }

        public void setNoCache(boolean noCache) {
            this.noCache = noCache;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}