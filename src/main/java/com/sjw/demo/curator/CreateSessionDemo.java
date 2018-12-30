package com.sjw.demo.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
/**
 * Curator创建一个session连接的基本使用方法
 */
public class CreateSessionDemo {
	public static void main(String[] args) throws InterruptedException {
		RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
		//链式调用产生1个curator客户端
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(policy)
				.build();
		
		//开始连接
		client.start();
		Thread.sleep(Integer.MAX_VALUE);
	}
}
