package com.syj.bi.manager;

import com.syj.bi.common.ErrorCode;
import com.syj.bi.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author syj
 * @date 2024/9/16 23:31
 */

/**
 * 专门提供RedisLimiter 限流的基础服务的
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key){
        //创建一个名称为user_limiter的限流器，每秒最多访问2次
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //限流器的统计规则（每秒2个规则，连续的请求，最多只能有1个请求被允许通过）
        //RateType.OVERALL表示速率限制作用于整个令牌桶，即抑制所有请求的速率
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //每当操作来了之后请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1L);
        if(!canOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
