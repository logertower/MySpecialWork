package com.liutf.redis;

import com.liutf.util.CacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量清缓存利器
 */
public class CleanRedisKey {

    private static final ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

    /**
     * 清除key：customer_uid_
     */
    public static void cleanRedisKey(String key) {

        key = key + "*";
        String cursor = "0";
        do {
            ScanResult<String> scanResult = CacheUtil.scan(key, 100, cursor);
            cursor = scanResult.getStringCursor();
            List<String> stringList = scanResult.getResult();

            CacheUtil.del(StringUtils.join(stringList, ",").split(","));
            //System.out.println(StringUtils.join(stringList, ",").split(","));

            System.out.println(cursor);
            System.out.println(stringList.toString());

        } while (!"0".equals(cursor));


    }

    public static void main(String[] args) {
        System.out.println("-------------start----------all-------------start----------");
        List<String> keys = new ArrayList<>();
        keys.add("customer_uid_");
        keys.add("customer_idcard_");
        keys.add("customer:supplement:idcard:");
        keys.add("customer_openid_");
        keys.add("customer_mobile_");
        keys.add("member_level_point_level_match_list");
        keys.add("member:level:point:list:");
        for (String key : keys) {
            System.out.println("-------------start----------"+key+"-------------start----------");
            cleanRedisKey(key);
            System.out.println("-------------end----------"+key+"-------------end----------");
        }
        System.out.println("-------------start----------all-------------start----------");
    }


}
