package com.sjw.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
/**
 * Curator事件监听  包含NodeCache PathCache TreeCache
 * TreeCache 监听某一节点下包括自己的所有节点，也就是说：
 * 1.监听自己
 * 2.监听自己的所有后代
 * 
 * 同zkClient，Curator的事件监听也不是一次性的，只有原生的是一次性的
 */
public class TreeCacheDemo {
	public static void main(String[] args) throws Exception {
		String path = "/zk-client";
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		client.start();
		
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, "test".getBytes());

		//在构造器和start方法上，就比另外2个cache简单许多，没有多余的重载
		TreeCache tc = new TreeCache(client, path);
		tc.start();
			
		tc.getListenable().addListener(new TreeCacheListener() {
			//当触发监听器时，需执行的逻辑
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				String path = event.getData().getPath();
				String data = new String(event.getData().getData());
				Type type = event.getType();
				 switch (type) {
                 	case NODE_ADDED: {
                 		System.out.println("Node added: " + path + " data " + data);
                 		break;
                 	}
                 	case NODE_UPDATED: {
                 		System.out.println("Node changed: " + path + " data " + data);
                 		break;
                 	}
                 	case NODE_REMOVED: {
                 		System.out.println("Node removed: " + path + " data " + data);
                 		break;
                 	}
                 	//其他case如果有需要可以写出来
                 	default:
                 		break;
				 }
				
			}
		});
				
		//在监听启动完成后，且找到了目标节点，那么会自动触发一次 节点增加  的事件
		//（触发监听0）
		
		//更改自身数据内容，触发监听1
		//Thread.sleep--不能太过频繁的触发事件监听，可能与底层实现有关，如果去掉可能会导致触发次数不全
		//如下面5个Thread.sleep全部注释后，本机测试上只会触发4个
		client.setData().forPath(path,"test+test".getBytes());
		Thread.sleep(1000);
		
		String newPath = path + "/treecache";
			
		//增加一级子节点，触发监听2
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(newPath, "test1".getBytes());
		Thread.sleep(1000);
		
		//更改一级子节点内容，触发监听3
		client.setData().forPath(newPath,"test111".getBytes());
		Thread.sleep(1000);
		
		//增加二级子节点，触发监听4
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(newPath + "/newnewpath", "test2".getBytes());
		Thread.sleep(1000);
			
		//删除二级子节点，触发监听5
		client.delete().deletingChildrenIfNeeded().forPath(newPath + "/newnewpath");
		Thread.sleep(1000);
		
		tc.close();
	}
}
