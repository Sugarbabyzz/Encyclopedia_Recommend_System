package casia.isiteam.recommendsystem.algorithms.all.latest;

import casia.isiteam.recommendsystem.algorithms.RecommendAlgorithm;
import casia.isiteam.recommendsystem.main.Recommender;
import casia.isiteam.recommendsystem.utils.DBKit;
import casia.isiteam.recommendsystem.utils.RecommendKit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class LatestRecommender implements RecommendAlgorithm {

    private static final Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    public static Map<Integer, List<Long>> latestItemsMap = new HashMap<>();

    @Override
    public void recommend(List<Long> userIDs, int infoType) {
        logger.info("信息类型：" + infoType + "  基于最新推荐 开始于 " + new Date());
        for (Long userID : userIDs) {
            // 初始化
            RecommendKit.initToBeRecommended(userID, infoType);
            // 添加生成的推荐词条项
            int i = latestItemsMap.get(infoType).size();
            while (i-- > 0) {
                Recommender.toBeRecommended.get(userID).get(infoType).add(latestItemsMap.get(infoType).get(i));
            }
        }
        logger.info("信息类型：" + infoType + "  基于最新推荐 结束于 " + new Date());
    }

    // 生成最新项
    public static void formLatestItems() {
        // 获取每个类型的随机项
        logger.info("正在生成 最新项....");
        for (Integer infoType : Recommender.infoTypes) {
            // 生成随机项
            latestItemsMap.put(infoType, new ArrayList<>());
            List<Long> latestItemIDs = DBKit.getItemsByDateAndInfoType(RecommendKit.getSpecificDayFormat(-1), infoType);
            latestItemsMap.get(infoType).addAll(latestItemIDs);
            // 添加到默认推荐项
            for (Long itemID : latestItemIDs) {
                Recommender.latestCandidates.add(new long[] {itemID, infoType});
            }
        }
        logger.info("生成 最新项 完毕！");
    }
}