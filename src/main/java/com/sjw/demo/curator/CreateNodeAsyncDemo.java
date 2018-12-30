package com.sjw.demo.curator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
/**
 * Curator异步通知
 * 此demo是异步创建节点
 */
public class CreateNodeAsyncDemo {
	static CountDownLatch cdl = new CountDownLatch(2);
	static ExecutorService es = Executors.newFixedThreadPool(2);

	public static void main(String[] args) throws Exception {
		String path = "/zk-client1";
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
		client.start();
		//inBackground后台创建，即异步
		//2个参数，第一个参数是回调接口，第二个参数是线程池
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
			@Override
			public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
				//result code包含（0成功，-4断开连接，-110节点已存在，-112会话过期）
				//type包括CREATE、DELETE、EXISTS、GET_DATA、SET_DATA、CHILDREN、SYNC、GET_ACL、SET_ACL、WATCHED、CLOSING
				//表示调用的类型，此处是create()
				System.out.println("result code: " + event.getResultCode() + ", type: " + event.getType());
				cdl.countDown();
			}
		}, es).forPath(path, "test1".getBytes());

		String path2 = "/zk-client2";
		//只传回调接口，不指定线程池，Curator会用EventThread去进行异步处理
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
			@Override
			public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
				System.out.println("result code: " + event.getResultCode() + ", type: " + event.getType());
				cdl.countDown();
			}
		}).forPath(path2, "test2".getBytes());

		cdl.await();
		es.shutdown();
		//能够输出end说明2个节点都已异步创建完成，此处由CountDownLatch来控制
		System.out.println("end");
	}
}
