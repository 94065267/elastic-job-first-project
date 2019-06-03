package com.jixs.elasticjob.test.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式job，定时扫描表的守护可以用此类型作业
 *
 * @author jixs
 * @date 2019/4/27
 */
@Slf4j
@Component
public class MyDataflowJob implements DataflowJob<String> {
    @Override
    public List<String> fetchData(ShardingContext shardingContext) {
        log.info("MyDataflowJob线程ID: {}, 任务总片数: {}, " +
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
        List<String> list = new ArrayList<>();
        list.add(shardingContext.getShardingItem() + ":" + shardingContext.getShardingParameter());
        return list;
    }

    @Override
    public void processData(ShardingContext shardingContext, List list) {

    }
}
