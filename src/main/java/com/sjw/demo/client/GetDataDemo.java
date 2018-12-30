package com.sjw.demo.client;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
/**
 * 使用ZKClient进行子节点列表监听
 */
public class GetDataDemo {
	public static void main(String[] args) throws InterruptedException {
		String path = "/zk-client";
		ZkClient client = new ZkClient("192.168.1.6:2181", 5000);
		//创建临时节点
		client.createEphemeral(path, "123");

		//监听数据变化
		client.subscribeDataChanges(path, new IZkDataListener() {
			//如果发生了数据修改，怎么处理修改
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				System.out.println(dataPath + " changed: " + data);
			}

			//如果发生了数据删除，怎么处理删除（数据被删除是由于节点被删除，把字符串置为空字符串或把对象置null不属于数据被删除，只属于改变，会调用到上1个方法）
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println(dataPath + " deleted");
			}
		});

		System.out.println(client.readData(path).toString());
		//修改数据将触发handleDataChange
		client.writeData(path, "456");
		Thread.sleep(1000);
		//删除节点，将会触发handleDataDeleted
		client.delete(path);
		
		client.createEphemeral(path + "/xixi");
		Thread.sleep(Integer.MAX_VALUE);
	}
}
