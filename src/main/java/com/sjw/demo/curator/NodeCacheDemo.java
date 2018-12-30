package com.sjw.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
/**
 * Curator事件监听  包含NodeCache PathCache TreeCache
 * NodeCache 只是监听某一个具体的单独的节点
 * 同zkClient，Curator的事件监听也不是一次性的，只有原生的是一次性的
 */
public class NodeCacheDemo {
	public static void main(String[] args) throws Exception {
		String path = "/zk-client/nodecache";
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		client.start();
		//注意：只有持久节点下才能创建临时节点，换句话说，临时节点下不能创建临时节点，这里Cuator会自动把/zk-client置为持久节点
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, "test".getBytes());

		//第三个参数代表是否压缩数据内容，默认是false，false的话也可直接使用2个参数的构造器
		NodeCache nc = new NodeCache(client, path, false);
		//任何一种Cache都是以start开始，close结束
		//PS：可以对不存在的节点进行监听，节点被创建时，将会触发第一次监听，以后的监听也自然正常
		//start方法参数默认为false，可以带true，设置为true首次不会触发监听事件（应该和监听不存在的节点有关系）
		nc.start();

		nc.getListenable().addListener(new NodeCacheListener() {
			//当触发监听器时，需执行的逻辑
			
			/*@Override
			public void nodeChanged() throws Exception {
				//注意：目前这个方法只能监听节点数据内容变更和节点被创建，无法监听是否被删除，如需同样监听，需完善该方法
				//可以回忆zkClient当时是怎么做的，没错，zkClient用的是2个方法，所以不存在该问题
				System.out.println("update--current data: " + new String(nc.getCurrentData().getData()));
			}*/
			
			@Override
			public void nodeChanged() throws Exception {
				ChildData data = nc.getCurrentData();
	            if (data != null) {
	                System.out.println("节点被创建或节点数据内容发生变更：" + new String(nc.getCurrentData().getData()));              
	            } else {
	                System.out.println("节点被删除!");
	            }
			}
		});
		
		//更新节点数据内容以触发监听器
		client.setData().forPath(path, "test123".getBytes());
		//Thread.sleep--不能太过频繁的触发事件监听，可能与底层实现有关，如果去掉可能会导致触发次数不全
		//比如本案例如果注释掉下一行，控制台将会0输出！
		Thread.sleep(1000);
		
		//删除触发监听器
		client.delete().deletingChildrenIfNeeded().forPath(path);
		Thread.sleep(1000);
		
		//对不存在的节点的监听
		//client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, "test".getBytes());
		//Thread.sleep(1000);
		nc.close();
	}
}
