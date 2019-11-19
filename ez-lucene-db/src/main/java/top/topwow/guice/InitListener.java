package top.topwow.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import top.topwow.util.MyLog;

public class InitListener implements TypeListener {
	private static final Logger logger = Logger.getLogger(InitListener.class.toString());
	private static final ConcurrentHashMultiset<Class<?>> excludeClass = ConcurrentHashMultiset.create();

	@Override
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
		encounter.register(new InjectionListener<I>() {
			@Override
			public void afterInjection(Object obj) {
				if (excludeClass.contains(obj.getClass())) {
					return;
				}
				boolean find = false;
				for (Method m : obj.getClass().getMethods()) {
					for (Annotation a : m.getAnnotations()) {
						if (a instanceof PostConstruct) {
							try {
								find = true;
								m.invoke(obj);
							} catch (IllegalAccessException e) {
								logger.severe(MyLog
										.stackTrace(e, String.format("IllegalAccessException %s fail:", obj.getClass()))
										.toString());
							} catch (IllegalArgumentException e) {
								logger.severe(MyLog
										.stackTrace(e,
												String.format("IllegalArgumentException %s fail:", obj.getClass()))
										.toString());
							} catch (InvocationTargetException e) {
								logger.severe(MyLog
										.stackTrace(e,
												String.format("InvocationTargetException %s fail:", obj.getClass()))
										.toString());
							}
							break;
						}
					}
				}
				if (!find) {
					excludeClass.add(obj.getClass());
				}
			}
		});
	}
}
