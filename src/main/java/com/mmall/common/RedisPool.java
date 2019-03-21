package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool;//jedis 连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));//最大连接数
    private static Integer maxIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));//在JedisPool中最大的idle状态（空闲）的jedis实例的个数
    private static Integer minIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));//在JedisPool中最小的idle状态（空闲）的jedis实例的个数

    private static Boolean testOnBorrow =Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));;//在Borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true，则得到的jedis实例肯定是可以使用的
    private static Boolean testOnReturn =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","true"));;//在return一个jedis实例的时候，进行验证，如果是true表示还的jedis实例是可用的

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort =Integer.parseInt(PropertiesUtil.getProperty("redis.port"));


    private static void initPool(){
        JedisPoolConfig config  = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);//这个属性的默认值就是true，这里重新设置一遍的意思是为了强调重要性，连接耗尽的时候是否阻塞，fasle，抛出异常，true，阻塞直到超时

        pool = new JedisPool(config,redisIp,redisPort,1000*2);//超时时间的单位是ms

    }

    //这个类在加载到JVM的时候就要初始化这个连接池
    static{
        initPool();
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static  void returnResource(Jedis jedis){
        pool.returnResource(jedis);//源码中做了非空判断
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);//源码中做了非空判断
    }


//    public static void main(String[] args) {
//        Jedis jedis = pool.getResource();
//
//        jedis.set("testKey","testValue");
//
//        returnResource(jedis);
//
//        pool.destroy();//临时调用,销毁连接池中的所有连接
//        System.out.println("program is end");
//    }

}
