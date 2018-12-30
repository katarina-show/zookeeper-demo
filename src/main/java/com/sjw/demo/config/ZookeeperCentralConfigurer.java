package com.sjw.demo.config;

import java.util.List;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.zaxxer.hikari.HikariDataSource;
/**
 * ZK统一配置中心
 */
public class ZookeeperCentralConfigurer {
	private CuratorFramework zkClient;
	private TreeCache treeCache;

	//zk服务器IP地址+端口号
	private String zkServers;
	//配置中心的节点
	private String zkPath;
	//session超时事件
	private int sessionTimeout;
	//存放配置内容的Properties
	private Properties props;

	public ZookeeperCentralConfigurer(String zkServers, String zkPath, int sessionTimeout) {
		this.zkServers = zkServers;
		this.zkPath = zkPath;
		this.sessionTimeout = sessionTimeout;
		this.props = new Properties();

		initZkClient();
		getConfigData();
		addZkListener();
	}

	/*
	 * 构建一个连接
	 * */
	private void initZkClient() {
		zkClient = CuratorFrameworkFactory.builder()
				.connectString(zkServers)
				.sessionTimeoutMs(sessionTimeout)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
	    
		zkClient.start();
	}

	/*
	 * 获取配置数据
	 * */
	private void getConfigData() {
		try {
			//获取该节点下的所有子节点名称
			List<String> list = zkClient.getChildren().forPath(zkPath);
			for (String key : list) {
				//取出每个子节点的数据内容并存入Properties
				String value = new String(zkClient.getData().forPath(zkPath + "/" + key));
				if (value != null && value.length() > 0) {
					props.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 添加监听器
	 */
	private void addZkListener() {
		TreeCacheListener listener = new TreeCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				//节点内容更新
				if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
					getConfigData();
					WebApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
					HikariDataSource dataSource = (HikariDataSource) ctx.getBean("dataSource");
					System.out.println("================"+props.getProperty("url"));
					dataSource.setJdbcUrl(props.getProperty("url"));
				}
			}
		};

		treeCache = new TreeCache(zkClient, zkPath);
		try {
			treeCache.start();
			treeCache.getListenable().addListener(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Properties getProps() {
		return props;
	}

	public void setZkServers(String zkServers) {
		this.zkServers = zkServers;
	}

	public void setZkPath(String zkPath) {
		this.zkPath = zkPath;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
}
