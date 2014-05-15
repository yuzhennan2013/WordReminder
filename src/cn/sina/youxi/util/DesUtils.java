package cn.sina.youxi.util;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * des加密解密工具类
 * 
 */
public class DesUtils {

	public static final String THE_KEY = "7b0c8a76";

	// 采用DES的CBC模式进行加密，补齐方式为PKCS5Padding
	public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";

	public static byte[] encode(String key, byte[] data) {
		try {
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			// key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			IvParameterSpec iv = new IvParameterSpec(key.substring(0, 8)
					.getBytes());
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			return cipher.doFinal(data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}