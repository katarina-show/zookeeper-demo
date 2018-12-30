package com.sjw.demo.client;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * 使用ZKClient进行子节点列表监听
 */
public class GetChildrenDemo {
	
	public static void main(String[] args) throws InterruptedException {
		String path = "/zk-client";
		ZkClient client = new ZkClient("192.168.1.6:2181", 5000);
		//监听子节点变化(也包括自己)
		client.subscribeChildChanges(path, new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println(parentPath + "的子发生变化: " + currentChilds);
			}
		});

		client.createPersistent(path);
		Thread.sleep(1000);
		System.out.println(client.getChildren(path));
		client.createPersistent(path + "/c1");
		Thread.sleep(1000);
		client.delete(path + "/c1");
		Thread.sleep(1000);
		client.delete(path);
		Thread.sleep(Integer.MAX_VALUE);
	}
}
