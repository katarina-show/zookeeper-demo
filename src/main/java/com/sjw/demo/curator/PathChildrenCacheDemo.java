package com.sjw.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
/**
 * Curator事件监听  包含NodeCache PathCache TreeCache
 * PathCache 监听某一节点下的  一级子目录节点，也就是说：
 * 1.它不监听自己
 * 2.它不监听自己儿子的儿子
 * 
 * 同zkClient，Curator的事件监听也不是一次性的，只有原生的是一次性的
 */
public class PathChildrenCacheDemo {
	public static void main(String[] args) throws Exception {
		String path = "/zk-client";
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		client.start();
		
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, "test".getBytes());

		//第三个参数代表是否把节点数据放进Stat，一般情况下都是要放的，这样可以获取到节点数据内容
		PathChildrenCache pcc = new PathChildrenCache(client, path, true);
		//任何一种Cache都是以start开始，close结束
		//start也可以带参数，共有3种启动模式
		//StartMode.Normal  默认，异步方式启动
		//StartMode.POST_INITIALIZED_EVENT  异步方式启动，启动完成后，将会触发一次监听事件
		//StartMode.BUILD_INITIAL_CACHE  同步方式启动
		pcc.start();    //  =pcc.start(StartMode.NORMAL);
			
		pcc.getListenable().addListener(new PathChildrenCacheListener() {
			//当触发监听器时，需执行的逻辑
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				String path = event.getData().getPath();
				String data = new String(event.getData().getData());
				Type type = event.getType();
				 switch (type) {
				 	case INITIALIZED: {
				 		System.out.println("选用了StartMode.POST_INITIALIZED_EVENT的启动方式，现在已经启动完成，特此通告");
				 		break;
				 	}
                 	case CHILD_ADDED: {
                 		System.out.println("Node added: " + path + " data " + data);
                 		break;
                 	}
                 	case CHILD_UPDATED: {
                 		System.out.println("Node changed: " + path + " data " + data);
                 		break;
                 	}
                 	case CHILD_REMOVED: {
                 		System.out.println("Node removed: " + path + " data " + data);
                 		break;
                 	}
                 	//其他case如果有需要可以写出来
                 	default:
                 		break;
				 }
			}
		});
			
		//更改自身数据内容，不触发监听
		client.setData().forPath(path,"test+test".getBytes());
		
		String newPath = path + "/pathchildrencache";
		
		//Thread.sleep--不能太过频繁的触发事件监听，可能与底层实现有关，如果去掉可能会导致触发次数不全
		//如本案例去掉下方3个Thread.sleep(1000)，本机测试上将会导致update的监听无法触发
		//增加一级子节点，触发监听1add
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(newPath, "test1".getBytes());
		Thread.sleep(1000);
		
		//更改一级子节点内容，触发监听2update
		client.setData().forPath(newPath,"test111".getBytes());
		Thread.sleep(1000);
		
		//增加二级子节点，不触发监听
		client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(newPath + "/newnewpath", "test2".getBytes());
		
		//删除一级子节点，触发监听3remove
		client.delete().deletingChildrenIfNeeded().forPath(newPath);
		Thread.sleep(1000);
		
		pcc.close();
	}
}
