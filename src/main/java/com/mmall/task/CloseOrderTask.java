package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;


    //当使用shutdown关闭tomcat的时候，会执行这个方法，删除redis的锁防止发生死锁
    @PreDestroy
    public void delLock(){
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }


//    @Scheduled(cron = "0 */1 * * * ?")//一分钟执行一次
    public void closeOrderTaskV1(){
        log.info("关闭订单，定时任务启动");
        int hour= Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        log.info("关闭订单，定时任务结束");
    }

//    @Scheduled(cron = "0 */1 * * * ?")//一分钟执行一次
    public void closeOrderTaskV2(){
        log.info("关闭订单，定时任务启动");
        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeOut));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回值是1，代表设置成功，获取锁 关闭订单
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

        }else{
            log.info("没有获得分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单，定时任务结束");
    }


    @Scheduled(cron = "0 */1 * * * ?")//一分钟执行一次
    public void closeOrderTaskV3(){
        log.info("关闭订单，定时任务启动");
        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","50000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeOut));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回值是1，代表设置成功，获取锁 关闭订单
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeOut));
                //再次用当前时间戳getset
                //返回给定的key的旧值 ->使用旧值判断，是否可以获取锁
                //当key没有旧值时，即key不存在时，返回nil - >获取锁

                //这里我们set了一个新的value值，获取旧的值
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr,getSetResult))) {
                    //真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

                }else{
                    log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else{
                log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }

        }

        log.info("关闭订单，定时任务结束");
    }


    //改为0的原因是，如果时间任务执行的很快，那么第二个tomcat的时间任务在等待时间后也会拿到，但是时间任务只需要执行一次的
    //这里的getLock = 方法的执行，在if分支中会判定的说
//    @Scheduled(cron = "0 */1 * * * ?")//一分钟执行一次  使用的是redisson
    public void closeOrderTaskV4(){
        log.info("关闭订单，定时任务启动");

        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

        boolean getLock = false;
        try {
            if (getLock =lock.tryLock(0,5, TimeUnit.SECONDS)){
                log.info("Redisson获取分布式锁：{},ThreadName： {}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
                int hour= Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
                iOrderService.closeOrder(hour);
            }else {
                log.info("Redisson没有获取分布式锁：{},ThreadName： {}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
            }

        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常",e);
        }finally {
            if (getLock) {
                return;
            }

            lock.unlock();
            log.info("Redisson分布式锁释放锁");
        }

        log.info("关闭订单，定时任务结束");
    }

    private void closeOrder(String lockName){
        RedisShardedPoolUtil.expire(lockName,5);//有效期5s，防止死锁
        log.info("获取{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour= Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("获取{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("========================================================");
    }

    public static void main(String[] args) {
        boolean lock = false;
        if(lock = getLock()){
            System.out.println("true");
        }else {
            System.out.println("false");
        }
    }

    private static boolean getLock(){
        return false;
    }
}
