/**
 * Copyright @ 2016  shuibian Co. Ltd.
 * All right reserved.
 *
 * @author: Lijiannan
 * date: 	 2016年8月4日上午11:28:49
 */
package com.liutf.util;


import com.by.bimdb.service.RedisSentinelService;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Lijiannan
 * @version 1.0
 * @description
 * @create 2016年8月4日上午11:28:49
 */
@Service
public class CacheUtil {

    private static RedisSentinelService jedis;


    public static RedisSentinelService getJedis() {
        if (jedis == null) {
            jedis = SpringContextHolder.getBean("redisService", RedisSentinelService.class);
        }
        return jedis;
    }


    /**
     * @param key
     * @return 不存在返回null
     */
    public static String getString(String key) {
        return getJedis().get(key);
    }

    /**
     * @param key
     * @param value
     * @param seconds 时间，< 0 表示永不过期，单位 s
     * @return
     */
    public static boolean setString(String key, String value, int seconds) {

        if (seconds > 0)
            getJedis().setex(key, seconds, value);
        else
            getJedis().set(key, value);
        return true;
    }

    /**
     * 删除
     *
     * @param key
     * @return
     */
    public static boolean del(String key) {
        return getJedis().del(key) > 0;
    }

    /**
     * 批量删除
     *
     * @param keys
     * @return
     */
    public static Long del(String... keys) {
        return getJedis().del(keys);
    }

    /**
     * 计数
     *
     * @param key
     * @return
     */
    public static Long incr(String key) {
        return getJedis().incr(key);
    }

    /**
     * @param key
     * @param object
     * @param seconds 时间，< 0 表示永不过期，单位 s
     * @return
     */
    public static boolean set(String key, Object object, int seconds) {
        if (seconds > 0) {
            getJedis().setex((key).getBytes(), seconds, SerializationUtils.serialize((Serializable) object));
        } else {
            getJedis().set((key).getBytes(), SerializationUtils.serialize((Serializable) object));
        }
        return true;
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     *
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public static int setnx(String key, String value, int seconds) {

        long result = getJedis().setnx(key, value);
        if (result == 1) {
            if (seconds >= 0) {
                getJedis().expire(key, seconds);
            }
            return 1;
        }
        return 0;
    }

    /**
     * @param key
     * @return 不存在返回null
     */
    public static Object get(String key) {
        byte[] value = getJedis().get((key).getBytes());
        if (value == null)
            return null;
        return SerializationUtils.deserialize(value);
    }

    /**
     * 检查给定 key 是否存在。
     *
     * @param key
     * @return true or false
     */
    public static boolean exists(String key) {
        return getJedis().exists(key);
    }

    /**
     * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
     *
     * @param key
     * @return true or false
     */
    public static boolean expire(String key, int seconds) {
        if (seconds >= 0) {
            getJedis().expire(key, seconds);
        }
        return true;
    }

    /********************************* Redis 哈希 ******************************/
    /**
     * 根据一级key，获取所有的数据
     *
     * @param key 一级key
     * @return 不存在返回null
     */
    public static Map<String, Object> hget(String key) {
        Map<byte[], byte[]> values = getJedis().hgetAll(key.getBytes());
        if (values.size() == 0) {
            return null;
        } else {
            Map<String, Object> returnMap = new HashMap<String, Object>();
            for (Entry<byte[], byte[]> value : values.entrySet()) {
                returnMap.put(new String(value.getKey()), SerializationUtils.deserialize(value.getValue()));
            }
            return returnMap;
        }
    }

    /**
     * 根据一级key，二级key获取数据
     *
     * @param key   一级key
     * @param field 二级key
     * @return 不存在返回null
     */
    public static Object hget(String key, String field) {
        byte[] obj = getJedis().hget(key.getBytes(), field.getBytes());
        if (obj == null)
            return null;
        return SerializationUtils.deserialize(obj);
    }


    /**
     * 根据一级key，二级key获取数据
     *
     * @param key   一级key
     * @param field 二级key
     * @return 不存在返回null
     */
    public static String hgetString(String key, String field) {
        return getJedis().hget(key, field);
    }

    /**
     * 设置值
     *
     * @param key     一级key
     * @param field   二级key
     * @param value
     * @param seconds <0 表示永不过期，单位s，过期时间作用于一级key
     * @return
     */
    public static boolean hset(String key, String field, Object value, int seconds) {
        getJedis().hset(key.getBytes(), field.getBytes(), SerializationUtils.serialize((Serializable) value));
        if (seconds > 0) {
            getJedis().expire(key, seconds);
        }
        return true;
    }

    /**
     * 设置值
     *
     * @param key     一级key
     * @param field   二级key
     * @param value
     * @param seconds <0 表示永不过期，单位s，过期时间作用于一级key
     * @return
     */
    public static boolean hsetString(String key, String field, String value, int seconds) {
        getJedis().hset(key, field, value);
        if (seconds > 0) {
            getJedis().expire(key, seconds);
        }
        return true;
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中
     *
     * @param key
     * @param hash
     * @param seconds
     * @return
     */
    public static boolean hmset(String key, Map<String, String> hash, int seconds) {
        getJedis().hmset(key, hash);
        if (seconds > 0) {
            getJedis().expire(key, seconds);
        }
        return true;
    }

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。
     * 特别说明：需要传入参数和返回结果数量相等
     *
     * @param key
     * @param fields
     * @return
     */
    public static List<String> hmget(String key, String[] fields) {
        List<String> list = getJedis().hmget(key, fields);
        if (list == null) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < fields.length; i++) {
                result.add(null);
            }
            return result;
        }
        return list;
    }

    /**
     * 根据一级key，二级key删除数据
     *
     * @param key
     * @param field
     * @return
     */
    public static boolean hdel(String key, String field) {
        getJedis().hdel(key.getBytes(), field.getBytes());
        return true;
    }

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment 。
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static Long hincrby(String key, String field, long value) {
        return getJedis().hincrBy(key, field, value);
    }

    /******************************** mget ******************************/
    /**
     * @param keys
     * @return 不存在返回null
     */
    public static List<Object> mget(String[] keys) {
        if (keys == null || keys.length == 0 || keys[0] == null)
            return null;

        byte[][] bkeys = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            bkeys[i] = keys[i].getBytes();
        }
        List<byte[]> results = getJedis().mget(bkeys);
        List<Object> returnResult = new ArrayList<Object>();
        if (results.size() == 0)
            return null;
        for (byte[] result : results) {
            if (result == null)
                continue;
            returnResult.add(SerializationUtils.deserialize(result));
        }
        return returnResult;
    }

    /**
     * 设置缓存，有效时间为unix时间戳
     *
     * @param key
     * @param object
     * @param timestamp unix时间戳
     * @return
     */
    public static boolean setExpireAt(String key, Object object, long timestamp) {
        getJedis().set((key).getBytes(), SerializationUtils.serialize((Serializable) object));
        getJedis().expireAt(key, timestamp);
        return true;
    }

    /**
     * 剩余有效期
     *
     * @param key
     * @return 不存在的key：-1
     */
    public static Long ttl(String key) {
        return getJedis().ttl(key);
    }
    
    /**
     * 
     * @param key  模糊匹配的key
     * @param count 每次获取最大个数
     * @param cursor
     * @return
     */
    public static ScanResult<String> scan(String key, Integer count, String cursor) {
	    ScanParams params = new ScanParams();
	    params.match(key);
	    params.count(count);
	    return getJedis().scan(cursor, params);
    }

}
