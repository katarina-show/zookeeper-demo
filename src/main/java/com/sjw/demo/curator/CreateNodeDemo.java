package com.sjw.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
/**
 * Curator创建节点
 */
public class CreateNodeDemo {
	public static void main(String[] args) throws Exception {
		String path = "/zk-client/c1";

		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		
		client.start();
			
		//注：如果没有设置节点类型和内容，默认是持久节点，内容默认是空，creatingParentsIfNeeded表示如果有多个节点会自动创建父节点
		//client.create().creatingParentsIfNeeded().forPath(path);
		
		//通过CreateMode.xxx可以创建不同类型的节点
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, "test".getBytes());
		
		//检查节点是否存在
		Stat stat = client.checkExists().forPath(path);
		System.out.println(stat == null ? "不存在" + path +"节点" : "存在" + path +"节点");
	}
}
