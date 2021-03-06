package casia.isiteam.recommendsystem.utils;

import casia.isiteam.recommendsystem.algorithms.all.hr.HotRecommender;
import casia.isiteam.recommendsystem.algorithms.all.latest.LatestRecommender;
import casia.isiteam.recommendsystem.algorithms.all.random.RandomRecommender;
import casia.isiteam.recommendsystem.main.Recommender;
import casia.isiteam.recommendsystem.model.Item;
import casia.isiteam.recommendsystem.model.ItemLog;
import casia.isiteam.recommendsystem.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 供算法调用的工具方法
 */
public class RecommendKit {

    private static final Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    // 推荐信息项的时效性天数，即从推荐当天开始到之前的 beforeDays 天的信息项仍然具有时效性，予以推荐
    private static final int beforeDays = ConfigKit.getInt("RecommendBeforeDays");

    /**
     * 在当日基础上增加/减少天数后的日期字符串
     * @return yyyy-MM-dd 日期字符串
     */
    public static String getInRecDate() {
        return getSpecificDayFormat(beforeDays);
    }

    /**
     * 在当日基础上增加/减少天数后的日期字符串
     * @param beforeDays 增加/减少的天数
     * @return yyyy-MM-dd 日期字符串
     */
    public static String getInRecDate(int beforeDays) {
        return getSpecificDayFormat(beforeDays);
    }

    /**
     * getInRecDate 方法的具体实现
     */
    public static String getSpecificDayFormat(int beforeDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, beforeDays);  // 增加/减少天数，取决beforeDays的正负
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取特定的时间戳
     */
    public static Timestamp getCertainTimestamp(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return new Timestamp(calendar.getTime().getTime());
    }

    /**
     * 获取 用户与偏好列表 的键值对
     * @param userIDs 用户ID列表
     * @param infoType 类型
     */
    public static Map<Long, String> getUserPreListMap(Collection<Long> userIDs, int infoType) {
        Map<Long, String> map = new HashMap<>();
        List<User> users = DBKit.getUserPrefList(userIDs, infoType);
        users.forEach(user ->
            map.put(user.getId(), RecommendKit.getPrefList(user, infoType))
        );
        return map;
    }

    /**
     * 根据 infoType 获取 user 的偏好
     */
    public static String getPrefList(User user, int infoType) {
        switch (infoType) {
            case 1:  return user.getPref_list();
            case 2:  return user.getWiki_pref_list();
            case 3:  return user.getPeriodical_pref_list();
            case 4:  return user.getReport_pref_list();
            case 5:  return user.getSubject_pref_list();
            default:  return "";
        }
    }

    /**
     * 根据 infoType 获取 item 的 id
     */
    public static Long getItemId(Item item, int infoType) {
        switch (infoType) {
            case 1:
            case 3:
            case 4:  return item.getId();
            case 2:  return item.getWiki_info_id();
            case 5:  return item.getAuto_id();
            default:  return 0L;
        }
    }

    /**
     * 根据 infoType 获取 item 的 name
     */
    public static String getItemName(Item item, int infoType) {
        switch (infoType) {
            case 1:  return item.getInfoTitle();
            case 2:  return item.getName();
            case 3:  return item.getPerName();
            case 4:  return item.getReportName();
            case 5:  return item.getSubjectName();
            default:  return "";
        }
    }

    /**
     * 过滤用户已经看过的信息项
     * @param recItemList 信息项推荐列表
     * @param userID 用户ID
     * @param infoType 类型
     */
    public static void filterBrowsedItems(Collection<Long> recItemList, Long userID, int infoType) {
        if (recItemList.size() == 0)  return;
        List<ItemLog> userBrowsedItems = DBKit.getUserBrowsedItems(userID, infoType);
        userBrowsedItems.forEach(itemLog ->
            recItemList.remove(itemLog.getRef_data_id())
        );
    }
    /**
     * 去除推荐结果中超出数量限制的部分
     * @param toBeRecommended 推荐候选列表
     * @param recNum 最大推荐数量限制
     */
    public static void removeOverSizeItems(Collection<Long> toBeRecommended, int recNum) {

        if (toBeRecommended.size() <= recNum)  return;
        int i = 0;
        Iterator<Long> iterator = toBeRecommended.iterator();
        while (iterator.hasNext()) {
            if (i >= recNum) {
                iterator.remove();
            }
            iterator.next();
            i++;
        }
    }

    /**
     * 初始化 toBeRecommended
     */
    public static void initToBeRecommended(Long userID, int infoType) {
        Recommender.toBeRecommended.computeIfAbsent(userID, k -> new HashMap<>());
        Recommender.toBeRecommended.get(userID).computeIfAbsent(infoType, k -> new HashSet<>());
    }

    /**
     * 推荐项初始化
     */
    public static void emptyRecommendations() {
        Recommender.toBeRecommended.clear();
        Recommender.defaultCandidates.clear();
        Recommender.latestCandidates.clear();
        HotRecommender.hotItemsMap.clear();
        LatestRecommender.latestItemsMap.clear();
        RandomRecommender.randomItemsMap.clear();
    }
}
