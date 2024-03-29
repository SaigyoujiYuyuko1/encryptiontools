package com.example.asuredelete.Utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
@Slf4j
public class RSASign {

   private static KeyPairGenerator keyPairGenerator;
    private static Signature signature;
    private static KeyFactory keyFactory;
    static {
        try {
            keyPairGenerator = KeyPairGenerator
                    .getInstance("RSA");
        keyFactory = KeyFactory.getInstance("RSA");
    //设置KEY的长度
            keyPairGenerator.initialize(512);
            signature= Signature.getInstance("MD5withRSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    static KeyPair keyPair = keyPairGenerator.generateKeyPair();

    public static RSAPublicKey getPublicKey(){
        return (RSAPublicKey) keyPair.getPublic();
    }
    public static RSAPrivateKey getPrivateKey(){
        return  (RSAPrivateKey) keyPair.getPrivate();
    }

    @SneakyThrows
    public static byte[] signFile(RSAPrivateKey rsaPrivateKey,byte[] file){
        if (rsaPrivateKey==null){
            log.info("rsaPrivateKey为空");
            rsaPrivateKey=(RSAPrivateKey) keyPair.getPrivate();
        }
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded());
        //构造一个privateKey
        PrivateKey privateKey = keyFactory
                .generatePrivate(pkcs8EncodedKeySpec);
        //声明签名的对象
        signature.initSign(privateKey);
        signature.update(file);
        //进行签名
        return signature.sign();

    }
    @SneakyThrows
    public static boolean verifySign(RSAPublicKey rsaPublicKey,byte[] result ,byte[] file){
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                rsaPublicKey.getEncoded());
        keyFactory = KeyFactory.getInstance("RSA");
        //构造一个publicKey
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        //声明签名对象
        signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicKey);
        signature.update(file);
        //验证签名
       return signature.verify(result);
    }

    @SneakyThrows
    public static String encryptByPublicKey(byte[] data,RSAPublicKey rsaPublicKey) {

        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE,rsaPublicKey);
        byte[] bytes = cipher.doFinal(data);
        return Base64Utils.encodeToString(bytes);
    }

    /**
     * 私钥解密
     * @param data 待解密数据
     * @return byte[] 解密数据
     * */
    @SneakyThrows
    public static String decryptByPrivateKey(byte[] data,RSAPrivateKey rsaPrivateKey) {
        //数据解密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        return new String(cipher.doFinal(data));
    }


}
