package top.topwow.util;

public class MyLog {
	public static StringBuilder stackTrace(Exception e, String... msg) {
		StringBuilder sb = MyLog.trace(e);
		for (String s : msg) {
			sb.append(s).append('\r').append('\n');
		}

		return sb;
	}

	private static StringBuilder trace(Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append('\r').append('\n');
		StackTraceElement[] stack = e.getStackTrace();

		boolean added = false;
		int i = 0;
		for (StackTraceElement s : stack) {
			if ((++i) < 5) {
				added = true;
				sb.append(s.getClassName()).append('.').append(s.getMethodName()).append('.').append(s.getLineNumber())
						.append('\r').append('\n');
			}
		}

		if (!added && stack.length > 0) {
			StackTraceElement s = stack[0];
			sb.append(s.getClassName()).append('.').append(s.getMethodName()).append('.').append(s.getLineNumber())
					.append('\r').append('\n');
		}
		return sb;
	}
}
