package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {

    private static ShardedJedisPool pool;//sharded jedis 连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));//最大连接数
    private static Integer maxIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));//在JedisPool中最大的idle状态（空闲）的jedis实例的个数
    private static Integer minIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));//在JedisPool中最小的idle状态（空闲）的jedis实例的个数

    private static Boolean testOnBorrow =Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));;//在Borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true，则得到的jedis实例肯定是可以使用的
    private static Boolean testOnReturn =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","true"));;//在return一个jedis实例的时候，进行验证，如果是true表示还的jedis实例是可用的

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port =Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));

    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port =Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));


    private static void initPool(){
        JedisPoolConfig config  = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);//这个属性的默认值就是true，这里重新设置一遍的意思是为了强调重要性，连接耗尽的时候是否阻塞，fasle，抛出异常，true，阻塞直到超时

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip,redis1Port,1000 * 2);
//        info1.setPassword(); //如果有密码的话
//        JedisShardInfo info2 = new JedisShardInfo(redis2Ip,redis2Port,2000 * 2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);
        jedisShardInfoList.add(info1);
//        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config,jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    //这个类在加载到JVM的时候就要初始化这个连接池
    static{
        initPool();
    }

    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    public static  void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);//源码中做了非空判断
    }

    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);//源码中做了非空判断
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for (int i = 0; i < 10; i++) {
            jedis.set("key"+i,"value"+i);
        }
        returnResource(jedis);
        System.out.println("program is end");
    }
}
