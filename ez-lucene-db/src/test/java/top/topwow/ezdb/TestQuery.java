package top.topwow.ezdb;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;

import top.topwow.service.search.IndexManager;
import top.topwow.service.search.Page;
import top.topwow.util.Utils;

public class TestQuery {

	private IndexManager indexManager;

	@Before
	public void setUp() {
		indexManager = App.getInject().getInstance(IndexManager.class);
	}

	public void write() throws InterruptedException {

		for (int i = 0; i < 200; i++) {
			new Thread(() -> {
				try {
					Document doc = new Document();
					doc.add(new StringField("id", Utils.getUUID(), Store.YES));
					doc.add(new StringField("content", "Document content:" + Utils.getUUID(), Store.YES));
					doc.add(new LongPoint("time", System.currentTimeMillis()));
					indexManager.insert("testindex", doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
		sleep(2000);
		System.out.println("写入完成!");
	}

	/**
	 * 
	 * Occur.MUST_NOT条件不能仅包含自己
	 */

	@Test
	public void query() throws InterruptedException {

		write();// 先写入数据，再边查询边删除

		for (int i = 0; i < 2; i++) {
			new Thread(new MyTask(i)).start();
		}

		sleep(2000000);
		System.out.println("查询完成!");
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public class MyTask implements Runnable {
		public int thread;

		public MyTask(int thread) {
			this.thread = thread;
		}

		@Override
		public void run() {
			try {

				Query query = LongPoint.newRangeQuery("time", 0, System.currentTimeMillis());

				int count = 0;
				for (int p = 1; p < 5; p++) { // 共查询4页
					Page page = new Page(p, 3); // 每页3条记录
					List<Document> docs = indexManager.query("testindex", query, page);
					if (docs.isEmpty()) {
						System.out.println("线程" + thread + "，第" + page.getPage() + "页" + "没有查询到数据。");
						continue;
					}
					for (Document doc : docs) {
						System.out.println("线程" + thread + ", 共" + page.getTotal() + "记录，" + page.getPage() + "页"
								+ (++count) + "," + doc.get("content"));
					}

					if (p > 2) {
						// 先删除全部，再看看查询结果
						indexManager.delete("testindex",
								LongPoint.newRangeQuery("time", 0, System.currentTimeMillis()));
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
