package com.syj.bi.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author syj
 * @date 2024/9/21 16:13
 */
@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  发送消息
     * @param exchange 交换机名称
     * @param routingKey 路由键名称
     * @param msg 消息
     */
    public void  sendMessage(String exchange,String routingKey,String msg){
        rabbitTemplate.convertAndSend(exchange,routingKey,msg);
    }
}
