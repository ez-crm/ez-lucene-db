package top.topwow.ezdb;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

import top.topwow.config.Config;
import top.topwow.guice.InitListener;
import top.topwow.service.search.DB;

public class DBCon implements Module {
	private static Injector inject;
	private static DB db;

	public static DB getDB() {
		if (db == null) {
			db = DBCon.getInject().getInstance(DB.class);
		}
		return db;
	}

	public static Injector getInject() {
		if (inject != null) {
			return inject;
		}
		synchronized (DBCon.class) {
			if (inject == null) {
				inject = Guice.createInjector(new DBCon());
			}
		}
		return inject;
	}

	@Override
	public void configure(Binder binder) {

		binder.bindListener(Matchers.any(), new InitListener());

		binder.bind(DB.class);
		binder.bind(Config.class);

	}
}
