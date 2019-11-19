package top.topwow.ezdb;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.matcher.Matchers;

import top.topwow.config.Config;
import top.topwow.guice.InitListener;
import top.topwow.service.search.IndexManager;

public class App implements Module {
	private static Injector inject;
	private static IndexManager indexManager;

	public static IndexManager getIndexManager() {
		if (indexManager == null) {
			indexManager = App.getInject().getInstance(IndexManager.class);
		}
		return indexManager;
	}

	public static Injector getInject() {
		if (inject != null) {
			return inject;
		}
		synchronized (App.class) {
			if (inject == null) {
				inject = Guice.createInjector(new App());
			}
		}
		return inject;
	}

	@Override
	public void configure(Binder binder) {

		binder.bindListener(Matchers.any(), new InitListener());

		binder.bind(IndexManager.class);
		binder.bind(Config.class);

	}
}
