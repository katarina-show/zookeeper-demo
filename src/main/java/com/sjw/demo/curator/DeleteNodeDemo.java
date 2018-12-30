package com.sjw.demo.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
/**
 * Curator删除节点
 */
public class DeleteNodeDemo {
	public static void main(String[] args) throws Exception {
		String path = "/zk-client/c1";
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("192.168.1.6:2181")
				.sessionTimeoutMs(5000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		client.start();
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, "test".getBytes());
		
		//1.删除一个节点（该方法只适用于叶子节点，比如这边如果 delete().forPath("/zk-client")会报错）
		//client.delete().forPath(path);
		
		//2.删除一个节点，并且递归删除其所有的子节点，解决1的问题
		//client.delete().deletingChildrenIfNeeded().forPath(path);
		
		//3.删除一个节点，指定版本进行删除
		//client.delete().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
		//版本号哪里来？Stat类包含了1个节点的所有信息，如事务id（zxid）、版本号（version）等
		//版本号默认是0，每次操作节点数据使版本号加1，-1代表最新的版本号
		
		Stat stat = new Stat();
		//把状态信息存入Stat对象
		client.getData().storingStatIn(stat).forPath(path);

		
		client.delete().deletingChildrenIfNeeded().withVersion(stat.getVersion()).forPath(path);
		System.out.println("删除了版本号为" + stat.getVersion() + "的节点");
		
		//4.担保删除 --只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到删除节点成功。
		//client.delete().guaranteed().forPath(path);
		
		//自由组合：client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(-1).forPath(path);
		
	}
}
