package com.sjw.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
/**
 * Curator更新节点
 */
public class UpdateDataDemo {
	public static void main(String[] args) throws Exception {
		String path = "/zk-client/c1";
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		
		client.start();
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, "test".getBytes());
		
		//1.更新一个节点的数据内容
		//client.setData().forPath(path,"test1".getBytes());
		
		//2.更新一个节点的数据内容，指定版本进行更新
		//client.setData().withVersion(-1).forPath(path,"test1".getBytes());
		//版本号哪里来？Stat类包含了1个节点的所有信息，如事务id（zxid）、版本号（version）等
		//版本号默认是0，每次操作节点数据使版本号加1，-1代表最新的版本号
		
		Stat stat = new Stat();

		client.getData().storingStatIn(stat).forPath(path);
		System.out.println("Current version: " + stat.getVersion());
		System.out.println("Update version: "
				+ client.setData().withVersion(stat.getVersion()).forPath(path, "some".getBytes()).getVersion());
	}
}
