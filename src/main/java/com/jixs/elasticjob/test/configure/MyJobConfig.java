package com.jixs.elasticjob.test.configure;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author jixs
 * @date 2019/4/26
 */
@Configuration
@EnableConfigurationProperties({ElasticJobProperties.class})
@ConditionalOnProperty("elasticjob.zookeeper.server-lists")
public class MyJobConfig {

    @Autowired
    private ElasticJobProperties elasticJobProperties;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, JobProperties> jobMap = elasticJobProperties.getJobMap();
        if (jobMap != null && !jobMap.isEmpty()) {
            //获取实现了ElasticJob接口的所有bean
            Map<String, ElasticJob> elasticJobBeanMap = applicationContext.getBeansOfType(ElasticJob.class);
            if (elasticJobBeanMap != null && !elasticJobBeanMap.isEmpty()) {
                for (Map.Entry<String, ElasticJob> elasticJobBean : elasticJobBeanMap.entrySet()) {
                    Class<? extends ElasticJob> jobClass = elasticJobBean.getValue().getClass();
                    JobProperties jobProperties = jobMap.get(elasticJobBean.getKey());
                    //守护进程设置StreamingProcess为true
                    if (elasticJobBean.getValue() instanceof SimpleJob || elasticJobBean.getValue() instanceof DataflowJob) {
                        jobProperties.setStreamingProcess(true);
                    }

                    LiteJobConfiguration liteJobConfiguration = getLiteJobConfiguration(elasticJobBean.getValue(), jobProperties);
                    //注册作业
                    new SpringJobScheduler(elasticJobBean.getValue(), regCenter(), liteJobConfiguration).init();
                }
            }
        }
    }

    /**
     * 作业配置
     * 作业配置分为3级，分别是JobCoreConfiguration，JobTypeConfiguration和LiteJobConfiguration。
     * LiteJobConfiguration使用JobTypeConfiguration，JobTypeConfiguration使用JobCoreConfiguration，层层嵌套。
     * JobTypeConfiguration根据不同实现类型分为SimpleJobConfiguration，DataflowJobConfiguration和ScriptJobConfiguration。
     *
     * @param elasticJob
     * @param jobProperties
     * @return
     */
    private LiteJobConfiguration getLiteJobConfiguration(final ElasticJob elasticJob,
                                                         final JobProperties jobProperties) {
        //获取任务的实现类
        Class<? extends ElasticJob> jobClass = elasticJob.getClass();
        // 定义作业核心配置
        JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                .newBuilder(jobClass.getName(), jobProperties.getCron(), jobProperties.getShardingTotalCount())
                .shardingItemParameters(jobProperties.getShardingItemParameters())
                .jobParameter(jobProperties.getJobParameter())
                .description(jobProperties.getDescription())
                .build();
        // 定义类型配置

        JobTypeConfiguration jobTypeConfiguration;
        if (elasticJob instanceof DataflowJob) {
            jobTypeConfiguration = new DataflowJobConfiguration(jobCoreConfiguration, jobClass.getCanonicalName(), jobProperties.isStreamingProcess());
        } else if (elasticJob instanceof ScriptJob) {
            jobTypeConfiguration = new ScriptJobConfiguration(jobCoreConfiguration, jobProperties.getScriptCommandLine());
        } else {
            jobTypeConfiguration = new SimpleJobConfiguration(jobCoreConfiguration, jobClass.getCanonicalName());
        }
        // 定义Lite作业根配置
        return LiteJobConfiguration.newBuilder(jobTypeConfiguration).overwrite(true).build();

    }

    /**
     * zk注册
     *
     * @return zk注册中心
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter regCenter() {
        ZookeeperRegistryCenterProperties zookeeper = elasticJobProperties.getZookeeper();
        ZookeeperConfiguration zkcfg = new ZookeeperConfiguration(zookeeper.getServerLists(), zookeeper.getNamespace());
        zkcfg.setBaseSleepTimeMilliseconds(zookeeper.getBaseSleepTimeMilliseconds());
        zkcfg.setConnectionTimeoutMilliseconds(zookeeper.getConnectionTimeoutMilliseconds());
        zkcfg.setMaxSleepTimeMilliseconds(zookeeper.getMaxSleepTimeMilliseconds());
        zkcfg.setSessionTimeoutMilliseconds(zookeeper.getSessionTimeoutMilliseconds());
        zkcfg.setMaxRetries(zookeeper.getMaxRetries());
        zkcfg.setDigest(zookeeper.getDigest());
        return new ZookeeperRegistryCenter(zkcfg);
    }
}
