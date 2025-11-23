package com.mujin.commons.lang;



import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 加密解密工具类
 *
 * @author chenglin.wu
 * @date 2021/4/22
 */
@SuppressWarnings("unused")
public final class EncryptUtils {


    /**
     * 私有化构造器
     */
    private EncryptUtils() {

    }

    /**
     * 生成对称加密的秘钥(随机生成) 经过base64加密
     *
     * @param algorithm 加密算法
     * @return String 经过base64编码的aes加密秘钥
     * @date 2021/4/22
     */
    public static String generateKey(SymmetricAlgorithm algorithm) {
        SecretKey secretKey = SecureUtil.generateKey(algorithm.getValue());
        return base64Encode(secretKey.getEncoded());
    }

    // region AES加密解密

    /**
     * 通过AES加密数据(秘钥经过base64编码)
     *
     * @param content    加密数据
     * @param encryptKey 秘钥
     * @return String 返回base 64编码后的密文
     * @date 2025/11/23
     */
    public static String aesEncryptBase64Key(String content, String encryptKey) {
        byte[] bytes = aesEncryptToBytes(content, encryptKey, Boolean.TRUE);
        return base64Encode(bytes);
    }

    /**
     * 通过AES加密数据(秘钥没经过base64编码)
     *
     * @param content    加密数据
     * @param encryptKey 秘钥
     * @return String 返回base 64编码后的密文
     * @date 2025/11/23
     */
    public static String aesEncrypt(String content, String encryptKey) {
        byte[] bytes = aesEncryptToBytes(content, encryptKey, Boolean.FALSE);
        return base64Encode(bytes);
    }

    /**
     * 通过AES加密数据(秘钥没经过base64编码)
     *
     * @param content    加密数据
     * @param encryptKey 秘钥
     * @return String 返回base 64编码后的密文
     * @date 2025/11/23
     */
    public static String aesEncrypt(byte[] content, String encryptKey) {
        byte[] bytes = aesEncryptToBytes(content, encryptKey, Boolean.FALSE);
        return base64Encode(bytes);
    }

    /**
     * 通过AES加密数据(秘钥没经过base64编码)
     *
     * @param content    加密数据
     * @param encryptKey 秘钥
     * @return String 返回base 64编码后的密文
     * @date 2025/11/23
     */
    public static String aesEncryptBase64Key(byte[] content, String encryptKey) {
        byte[] bytes = aesEncryptToBytes(content, encryptKey, Boolean.TRUE);
        return base64Encode(bytes);
    }

    /**
     * 解密 (秘钥经过Base64编码)
     *
     * @param encryptStr 密文字符串
     * @param decryptKey 秘钥
     * @return String
     * @date 2021/4/22
     */
    public static String aesDecryptBase64Key(String encryptStr, String decryptKey) throws Exception {
        return aesDecryptByBytes(base64Decode(encryptStr), decryptKey, Boolean.TRUE);
    }

    /**
     * 解密 (秘钥没经过Base64编码)
     *
     * @param encryptStr 密文字符串
     * @param decryptKey 秘钥
     * @return String
     * @date 2021/4/22
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return aesDecryptByBytes(base64Decode(encryptStr), decryptKey, Boolean.FALSE);
    }

    /**
     * 获取 AES 实例
     *
     * @return AES
     * @date 2025/11/23
     */
    private static AES getAesCrypt() {
        return SecureUtil.aes();
    }

    /**
     * 获取 AES 实例
     *
     * @param key 秘钥
     * @return AES
     * @date 2025/11/23
     */
    private static AES getAesCrypt(String key) {
        return SecureUtil.aes(base64Decode(key));
    }

    /**
     * 获取 AES 实例
     *
     * @param key 秘钥
     * @return AES
     * @date 2025/11/23
     */
    private static AES getAesCrypt(byte[] key) {
        return SecureUtil.aes(key);
    }

    /**
     * 加密返回字节
     *
     * @param content     内容
     * @param encryptKey  秘钥
     * @param isBase64Key 秘钥是否经过base 64 编码
     * @return byte[]
     * @date 2021/4/22
     */
    private static byte[] aesEncryptToBytes(String content, String encryptKey, Boolean isBase64Key) {
        AES aes;
        if (isBase64Key) {
            aes = SecureUtil.aes(base64Decode(encryptKey));
        } else {
            aes = SecureUtil.aes(encryptKey.getBytes(StandardCharsets.UTF_8));
        }

        return aes.encrypt(content);
    }

    /**
     * 加密返回字节
     *
     * @param content     内容
     * @param encryptKey  秘钥
     * @param isBase64Key 秘钥是否经过base 64 编码
     * @return byte[]
     * @date 2021/4/22
     */
    private static byte[] aesEncryptToBytes(byte[] content, String encryptKey, Boolean isBase64Key) {
        AES aes;
        if (isBase64Key) {
            aes = SecureUtil.aes(base64Decode(encryptKey));
        } else {
            aes = SecureUtil.aes(encryptKey.getBytes(StandardCharsets.UTF_8));
        }

        return aes.encrypt(content);
    }

    /**
     * 解密
     *
     * @param encryptBytes 密文字节
     * @param decryptKey   秘钥
     * @return String
     * @date 2021/4/22
     */
    private static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey, Boolean isBase64Key) {
        AES aes;
        if (isBase64Key) {
            aes = SecureUtil.aes(base64Decode(decryptKey));
        } else {
            aes = SecureUtil.aes(decryptKey.getBytes(StandardCharsets.UTF_8));
        }
        byte[] decrypt = aes.decrypt(encryptBytes);
        return new String(decrypt, StandardCharsets.UTF_8);
    }

    // endregion

    // region base64加密解密

    /**
     * Base64 将byte数组加密成String
     *
     * @param bytes 字节数组
     * @return String
     * @date 2021/4/22
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Base64将String解密成byte数组
     *
     * @param base64Code 编码字符串
     * @return byte[]
     * @date 2021/4/22
     */
    public static byte[] base64Decode(String base64Code) {
        return Base64.getDecoder().decode(base64Code);
    }
    // endregion

    /**
     * sha1加密
     *
     * @param str 明文字符串
     * @return String
     * @date 2021/4/22
     */
    public static String toSha1(String str) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
