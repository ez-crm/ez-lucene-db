package top.topwow.ezdb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.junit.Before;
import org.junit.Test;

import top.topwow.service.search.IndexManager;
import top.topwow.util.Utils;

public class TestPerformace {
	private IndexManager indexManager = IndexManager.me;
	ThreadPoolExecutor executor;

	@Before
	public void setUp() {
		executor = new ThreadPoolExecutor(50, // 核心池大小
				50000, // 最大池大小
				200, // 线程最大空闲时间,超过此空闲时间可以被收回
				TimeUnit.MILLISECONDS, // 最大空闲时间的单位
				new ArrayBlockingQueue<Runnable>(10)// 用于保存执行任务的队列,10的意思是可以允许10个任务在排队，如果队列满了，则创建新的线程。
		);

	}

	/**
	10000个线程任务完毕，每个线程100条记录，提交了1000000条记录，查询到1000000 , 测试完成，耗费时间86秒。
	1000个线程任务完毕，每个线程1000条记录，提交了1000000条记录，查询到1000000，测试完成，耗费时间60秒。(推荐)
	100个线程任务完毕，每个线程10000条记录，提交了1000000条记录，查询到1000000，测试完成，耗费时间59秒。
	 */
	//@Test
	public void write() throws InterruptedException {

		long time = System.currentTimeMillis();

		int THREAD_NUM = 100;
		int RECORD_COUNT_PER_THREAD = 10000;

		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);

		for (int n = 0; n < THREAD_NUM; n++) {
			executor.execute(() -> {
				try {
					LinkedList<Document> docs = new LinkedList<>();
					for (int j = 0; j < RECORD_COUNT_PER_THREAD; j++) {
						Document doc = new Document();
						doc.add(new StringField("id", Utils.getUUID(), Store.YES));
						doc.add(new StringField("content", "文档内容：" + Utils.getUUID() + System.currentTimeMillis(),
								Store.YES));
						doc.add(new LongPoint("time", System.currentTimeMillis()));
						docs.add(doc);
					}
					indexManager.insert("testindex", docs);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		try {
			countDownLatch.await();
			System.out.print(THREAD_NUM + "个线程任务完毕，每个线程"+RECORD_COUNT_PER_THREAD+"条记录，提交了" + THREAD_NUM * RECORD_COUNT_PER_THREAD + "条记录，");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				long total = indexManager.queryTotal("testindex",
						LongPoint.newRangeQuery("time", 0, System.currentTimeMillis()));
				if (total >= THREAD_NUM * RECORD_COUNT_PER_THREAD) {
					System.out.println("查询到" + total + "，测试完成，耗费时间" + (System.currentTimeMillis() - time) / 1000 + "秒。");
					return;
				} else {
					System.out.println("查询到" + total);
				}

				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
