package com.sjw.demo.client;
/**
 * 阿里巴巴14年校招笔试题
 * 该类和zk没什么关系。。。
 */
public class TestJVM {
	public static int k = 0;

	public static TestJVM t1 = new TestJVM("t1");
	public static TestJVM t2 = new TestJVM("t2");
	public static int i = print("i");
	public static int n = 99;
	public int j = print("j");
	static {
		print("静态块");
	}

	public TestJVM(String str) {
		System.out.println("构造函数" + (++k) + ":" + str + "  i=" + i + " n=" + n);
		++i;
		++n;
	}

	{
		print("构造块");
	}

	public static int print(String str) {
		System.out.println("print方法" + (++k) + ":" + str + "  i=" + i + " n=" + n);
		++n;
		return ++i;
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		TestJVM t = new TestJVM("init");
	}
}
