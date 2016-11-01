/*
 * Copyright [2016-2026] wangcheng(wantedonline@outlook.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.wantedonline.puppy.httpserver.util.codec.crypt;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * RSA算法的封装类
 * 
 * @since 2010-8-10
 * @author hujiachao
 */
public class RSACrypt {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Cipher cipher;

    public RSACrypt() throws NoSuchAlgorithmException, NoSuchPaddingException {
        cipher = Cipher.getInstance("RSA");
    }

    /**
     * 生成密钥对
     * 
     * @param keysize RSA密钥长度(bit)
     */
    public void genKeyPairs(int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(keysize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
    }

    /**
     * 获取RSA私钥
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * 获取RSA私钥的bytes
     */
    public byte[] getPrivateKeyBytes() {
        if (null != privateKey) {
            return privateKey.getEncoded();
        }
        return null;
    }

    /**
     * 设置RSA私钥
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * 通过byte数组设置RSA私钥（编码方式为PKCS8）
     * 
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public void setPrivateKey(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(keySpec);
    }

    /**
     * 获取RSA公钥
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * 获取RSA公钥的bytes
     */
    public byte[] getPublicKeyBytes() {
        if (null != publicKey) {
            return publicKey.getEncoded();
        }
        return null;
    }

    /**
     * 设置RSA私钥
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * 通过byte数组设置RSA公钥（编码方式为X509）
     */
    public void setPublicKey(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(keySpec);
    }

    /**
     * 使用私钥加密
     */
    public byte[] encodeUsePrivateKey(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(input);
    }

    /**
     * 使用公钥加密
     */
    public byte[] encodeUsePublicKey(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(input);
    }

    /**
     * 使用私钥解密
     */
    public byte[] decodeUsePrivateKey(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(input);
    }

    /**
     * 使用公钥解密
     */
    public byte[] decodeUsePublicKey(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(input);
    }
}
