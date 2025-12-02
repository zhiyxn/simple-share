package com.simpleshare.article.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 现代化后台数据看板响应对象
 */
public class ArticleDashboardResponse {

    private Range range = new Range();
    private Overview overview = new Overview();
    private Trend trend = new Trend();
    private Funnel funnel = new Funnel();
    private SegmentCollection segments = new SegmentCollection();
    private Leaderboard leaderboards = new Leaderboard();
    private Moderation moderation = new Moderation();
    private List<Insight> insights = new ArrayList<>();

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }

    public Trend getTrend() {
        return trend;
    }

    public void setTrend(Trend trend) {
        this.trend = trend;
    }

    public Funnel getFunnel() {
        return funnel;
    }

    public void setFunnel(Funnel funnel) {
        this.funnel = funnel;
    }

    public SegmentCollection getSegments() {
        return segments;
    }

    public void setSegments(SegmentCollection segments) {
        this.segments = segments;
    }

    public Leaderboard getLeaderboards() {
        return leaderboards;
    }

    public void setLeaderboards(Leaderboard leaderboards) {
        this.leaderboards = leaderboards;
    }

    public Moderation getModeration() {
        return moderation;
    }

    public void setModeration(Moderation moderation) {
        this.moderation = moderation;
    }

    public List<Insight> getInsights() {
        return insights;
    }

    public void setInsights(List<Insight> insights) {
        this.insights = insights;
    }

    public static class Range {
        private String preset;
        private String startDate;
        private String endDate;

        public String getPreset() {
            return preset;
        }

        public void setPreset(String preset) {
            this.preset = preset;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class Overview {
        private long totalArticles;
        private long publishedArticles;
        private long draftArticles;
        private long privateArticles;
        private long reviewPendingArticles;
        private long reviewRejectedArticles;
        private long totalViews;
        private long totalLikes;
        private long totalCollects;
        private long totalComments;
        private long totalShares;
        private long weeklyNewArticles;
        private long todayNewArticles;
        private double viewGrowthRate;
        private double engagementGrowthRate;
        private LocalDateTime lastUpdated;

        public long getTotalArticles() {
            return totalArticles;
        }

        public void setTotalArticles(long totalArticles) {
            this.totalArticles = totalArticles;
        }

        public long getPublishedArticles() {
            return publishedArticles;
        }

        public void setPublishedArticles(long publishedArticles) {
            this.publishedArticles = publishedArticles;
        }

        public long getDraftArticles() {
            return draftArticles;
        }

        public void setDraftArticles(long draftArticles) {
            this.draftArticles = draftArticles;
        }

        public long getPrivateArticles() {
            return privateArticles;
        }

        public void setPrivateArticles(long privateArticles) {
            this.privateArticles = privateArticles;
        }

        public long getReviewPendingArticles() {
            return reviewPendingArticles;
        }

        public void setReviewPendingArticles(long reviewPendingArticles) {
            this.reviewPendingArticles = reviewPendingArticles;
        }

        public long getReviewRejectedArticles() {
            return reviewRejectedArticles;
        }

        public void setReviewRejectedArticles(long reviewRejectedArticles) {
            this.reviewRejectedArticles = reviewRejectedArticles;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public void setTotalViews(long totalViews) {
            this.totalViews = totalViews;
        }

        public long getTotalLikes() {
            return totalLikes;
        }

        public void setTotalLikes(long totalLikes) {
            this.totalLikes = totalLikes;
        }

        public long getTotalCollects() {
            return totalCollects;
        }

        public void setTotalCollects(long totalCollects) {
            this.totalCollects = totalCollects;
        }

        public long getTotalComments() {
            return totalComments;
        }

        public void setTotalComments(long totalComments) {
            this.totalComments = totalComments;
        }

        public long getTotalShares() {
            return totalShares;
        }

        public void setTotalShares(long totalShares) {
            this.totalShares = totalShares;
        }

        public long getWeeklyNewArticles() {
            return weeklyNewArticles;
        }

        public void setWeeklyNewArticles(long weeklyNewArticles) {
            this.weeklyNewArticles = weeklyNewArticles;
        }

        public long getTodayNewArticles() {
            return todayNewArticles;
        }

        public void setTodayNewArticles(long todayNewArticles) {
            this.todayNewArticles = todayNewArticles;
        }

        public double getViewGrowthRate() {
            return viewGrowthRate;
        }

        public void setViewGrowthRate(double viewGrowthRate) {
            this.viewGrowthRate = viewGrowthRate;
        }

        public double getEngagementGrowthRate() {
            return engagementGrowthRate;
        }

        public void setEngagementGrowthRate(double engagementGrowthRate) {
            this.engagementGrowthRate = engagementGrowthRate;
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }

    public static class Trend {
        private List<TrendPoint> points = new ArrayList<>();
        private long totalViews;
        private double averageViewsPerArticle;
        private double wowChange;

        public List<TrendPoint> getPoints() {
            return points;
        }

        public void setPoints(List<TrendPoint> points) {
            this.points = points;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public void setTotalViews(long totalViews) {
            this.totalViews = totalViews;
        }

        public double getAverageViewsPerArticle() {
            return averageViewsPerArticle;
        }

        public void setAverageViewsPerArticle(double averageViewsPerArticle) {
            this.averageViewsPerArticle = averageViewsPerArticle;
        }

        public double getWowChange() {
            return wowChange;
        }

        public void setWowChange(double wowChange) {
            this.wowChange = wowChange;
        }
    }

    public static class TrendPoint {
        private String date;
        private long published;
        private long views;
        private long likes;
        private long comments;
        private long shares;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getPublished() {
            return published;
        }

        public void setPublished(long published) {
            this.published = published;
        }

        public long getViews() {
            return views;
        }

        public void setViews(long views) {
            this.views = views;
        }

        public long getLikes() {
            return likes;
        }

        public void setLikes(long likes) {
            this.likes = likes;
        }

        public long getComments() {
            return comments;
        }

        public void setComments(long comments) {
            this.comments = comments;
        }

        public long getShares() {
            return shares;
        }

        public void setShares(long shares) {
            this.shares = shares;
        }
    }

    public static class Funnel {
        private List<FunnelStage> stages = new ArrayList<>();

        public List<FunnelStage> getStages() {
            return stages;
        }

        public void setStages(List<FunnelStage> stages) {
            this.stages = stages;
        }
    }

    public static class FunnelStage {
        private String key;
        private String label;
        private long value;
        private double conversion;
        private String hint;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public double getConversion() {
            return conversion;
        }

        public void setConversion(double conversion) {
            this.conversion = conversion;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }
    }

    public static class SegmentCollection {
        private List<SegmentItem> categories = new ArrayList<>();
        private List<SegmentItem> tags = new ArrayList<>();
        private List<SegmentItem> accessLevels = new ArrayList<>();

        public List<SegmentItem> getCategories() {
            return categories;
        }

        public void setCategories(List<SegmentItem> categories) {
            this.categories = categories;
        }

        public List<SegmentItem> getTags() {
            return tags;
        }

        public void setTags(List<SegmentItem> tags) {
            this.tags = tags;
        }

        public List<SegmentItem> getAccessLevels() {
            return accessLevels;
        }

        public void setAccessLevels(List<SegmentItem> accessLevels) {
            this.accessLevels = accessLevels;
        }
    }

    public static class SegmentItem {
        private String key;
        private String label;
        private long articleCount;
        private long viewCount;
        private long likeCount;
        private long commentCount;
        private long shareCount;
        private double percentage;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getArticleCount() {
            return articleCount;
        }

        public void setArticleCount(long articleCount) {
            this.articleCount = articleCount;
        }

        public long getViewCount() {
            return viewCount;
        }

        public void setViewCount(long viewCount) {
            this.viewCount = viewCount;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(long likeCount) {
            this.likeCount = likeCount;
        }

        public long getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(long commentCount) {
            this.commentCount = commentCount;
        }

        public long getShareCount() {
            return shareCount;
        }

        public void setShareCount(long shareCount) {
            this.shareCount = shareCount;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    public static class Leaderboard {
        private List<ArticleTile> topViews = new ArrayList<>();
        private List<ArticleTile> topEngagement = new ArrayList<>();
        private List<ArticleTile> latestPublished = new ArrayList<>();

        public List<ArticleTile> getTopViews() {
            return topViews;
        }

        public void setTopViews(List<ArticleTile> topViews) {
            this.topViews = topViews;
        }

        public List<ArticleTile> getTopEngagement() {
            return topEngagement;
        }

        public void setTopEngagement(List<ArticleTile> topEngagement) {
            this.topEngagement = topEngagement;
        }

        public List<ArticleTile> getLatestPublished() {
            return latestPublished;
        }

        public void setLatestPublished(List<ArticleTile> latestPublished) {
            this.latestPublished = latestPublished;
        }
    }

    public static class ArticleTile {
        private Long articleId;
        private String title;
        private String summary;
        private String authorName;
        private String categoryName;
        private Integer status;
        private Integer accessLevel;
        private boolean recommend;
        private boolean top;
        private boolean passwordProtected;
        private String publishTime;
        private long viewCount;
        private long likeCount;
        private long collectCount;
        private long commentCount;
        private long shareCount;
        private long engagement;

        public Long getArticleId() {
            return articleId;
        }

        public void setArticleId(Long articleId) {
            this.articleId = articleId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Integer getAccessLevel() {
            return accessLevel;
        }

        public void setAccessLevel(Integer accessLevel) {
            this.accessLevel = accessLevel;
        }

        public boolean isRecommend() {
            return recommend;
        }

        public void setRecommend(boolean recommend) {
            this.recommend = recommend;
        }

        public boolean isTop() {
            return top;
        }

        public void setTop(boolean top) {
            this.top = top;
        }

        public boolean isPasswordProtected() {
            return passwordProtected;
        }

        public void setPasswordProtected(boolean passwordProtected) {
            this.passwordProtected = passwordProtected;
        }

        public String getPublishTime() {
            return publishTime;
        }

        public void setPublishTime(String publishTime) {
            this.publishTime = publishTime;
        }

        public long getViewCount() {
            return viewCount;
        }

        public void setViewCount(long viewCount) {
            this.viewCount = viewCount;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(long likeCount) {
            this.likeCount = likeCount;
        }

        public long getCollectCount() {
            return collectCount;
        }

        public void setCollectCount(long collectCount) {
            this.collectCount = collectCount;
        }

        public long getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(long commentCount) {
            this.commentCount = commentCount;
        }

        public long getShareCount() {
            return shareCount;
        }

        public void setShareCount(long shareCount) {
            this.shareCount = shareCount;
        }

        public long getEngagement() {
            return engagement;
        }

        public void setEngagement(long engagement) {
            this.engagement = engagement;
        }
    }

    public static class Moderation {
        private long pendingReviews;
        private long rejectedReviews;
        private long oldestDraftDays;
        private List<ModerationRecord> records = new ArrayList<>();

        public long getPendingReviews() {
            return pendingReviews;
        }

        public void setPendingReviews(long pendingReviews) {
            this.pendingReviews = pendingReviews;
        }

        public long getRejectedReviews() {
            return rejectedReviews;
        }

        public void setRejectedReviews(long rejectedReviews) {
            this.rejectedReviews = rejectedReviews;
        }

        public long getOldestDraftDays() {
            return oldestDraftDays;
        }

        public void setOldestDraftDays(long oldestDraftDays) {
            this.oldestDraftDays = oldestDraftDays;
        }

        public List<ModerationRecord> getRecords() {
            return records;
        }

        public void setRecords(List<ModerationRecord> records) {
            this.records = records;
        }
    }

    public static class ModerationRecord {
        private Long articleId;
        private String title;
        private Integer reviewStatus;
        private Integer status;
        private String reviewer;
        private String updatedAt;

        public Long getArticleId() {
            return articleId;
        }

        public void setArticleId(Long articleId) {
            this.articleId = articleId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getReviewStatus() {
            return reviewStatus;
        }

        public void setReviewStatus(Integer reviewStatus) {
            this.reviewStatus = reviewStatus;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getReviewer() {
            return reviewer;
        }

        public void setReviewer(String reviewer) {
            this.reviewer = reviewer;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class Insight {
        private String title;
        private String description;
        private String severity;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }
}
