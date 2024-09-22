package com.syj.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author syj
 * @date 2024/9/21 16:49
 */

/**
 * 用于创建测试程序用到的交换机和队列
 */
public class MqInitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            String EXCHANGE_NAME = "code_exchange";
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            //创建队列，并指定队列名称
            String queueName  = "code_queue";
            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName,EXCHANGE_NAME,"my_routingKey");


        }catch (Exception e){

        }


    }
}
