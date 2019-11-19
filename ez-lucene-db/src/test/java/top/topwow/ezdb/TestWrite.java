package top.topwow.ezdb;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.junit.Before;
import org.junit.Test;

import top.topwow.service.search.IndexManager;
import top.topwow.util.Utils;

public class TestWrite {

	private IndexManager indexManager;

	@Before
	public void setUp() {
		indexManager = App.getInject().getInstance(IndexManager.class);
	}

	@Test
	public void write() throws InterruptedException {

		for (int i = 0; i < 200; i++) {
			new Thread(() -> {
				try {
					Document doc = new Document();
					doc.add(new StringField("id", Utils.getUUID(), Store.YES));
					doc.add(new StringField("content", "文档:" + Utils.getUUID(), Store.YES));
					doc.add(new LongPoint("time", System.currentTimeMillis()));
					indexManager.insert("testindex", doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

		}
		System.out.println("写入完成!");
		sleep(200000);

	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
