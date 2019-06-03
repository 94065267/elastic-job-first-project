package com.jixs.elasticjob.test.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简单job:收消息的守护可以用此类型作业
 *
 * @author jixs
 * @date 2019/4/26
 */
@Slf4j
@Component
public class MySimpleJob implements SimpleJob {
    @Override

    public void execute(ShardingContext shardingContext) {

        log.info("MySimpleJob线程ID: {}, 任务总片数: {}, " +
                        "当前分片项: {},当前参数: {}," +
                        "当前任务名称: {},当前任务参数: {}," +
                        "当前任务的id: {}",
                //获取当前线程的id
                Thread.currentThread().getId(),
                //获取任务总片数
                shardingContext.getShardingTotalCount(),
                //获取当前分片项
                shardingContext.getShardingItem(),
                //获取当前的参数
                shardingContext.getShardingParameter(),
                //获取当前的任务名称
                shardingContext.getJobName(),
                //获取当前任务参数
                shardingContext.getJobParameter(),
                //获取任务的id
                shardingContext.getTaskId());

    }
}
