package top.topwow.util;

import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

public class Utils {
	public static String getUUID() {
		return DigestUtils.md5Hex(UUID.randomUUID().toString());
	}
}
