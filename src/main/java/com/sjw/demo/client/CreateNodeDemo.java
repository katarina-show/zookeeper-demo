package com.sjw.demo.client;

import org.I0Itec.zkclient.ZkClient;

/**
 * 使用ZKClient创建节点
 */
public class CreateNodeDemo {
	public static void main(String[] args) {
		ZkClient client = new ZkClient("192.168.1.6:2181", 5000);
		String path = "/zk-client/c1";
		// 递归创建持久型节点，第二个参数需要为true
		client.createPersistent(path, true);
		System.out.println("创建成功");
	}
}
