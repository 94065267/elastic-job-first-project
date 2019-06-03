package com.jixs.elasticjob.test.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author jixs
 * @date 2019/4/26
 */
@Data
@ConfigurationProperties(prefix = "elasticjob")
public class ElasticJobProperties {
    /**
     * zk注册中心信息配置
     */
    private ZookeeperRegistryCenterProperties zookeeper;
    /**
     * 作业配置
     */
    private Map<String, JobProperties> jobMap;
}
