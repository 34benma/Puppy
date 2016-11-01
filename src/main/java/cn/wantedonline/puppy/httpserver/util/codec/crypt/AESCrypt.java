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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public final class AESCrypt {

    private SecretKey secretKey;
    private Cipher cipher;

    public AESCrypt() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("AES");
    }

    /**
     * 生成AES密钥（密钥长度为128bit）
     * 
     * @param seed
     * @throws NoSuchAlgorithmException
     */
    public void genSecretKey(byte[] seed) throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128, new SecureRandom(seed));
        setSecretKey(kg.generateKey());
    }

    /**
     * 获取AES密钥
     * 
     * @return
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * 获取AES密钥的byte数组
     * 
     * @return
     */
    public byte[] getSecretKeyBytes() {
        if (null != secretKey) {
            return secretKey.getEncoded();
        }
        return null;
    }

    /**
     * 设置AES密钥
     * 
     * @param secretKey
     */
    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 通过byte数组设置AES密钥
     * 
     * @param secretKeyBytes
     */
    public void setSecretKey(byte[] secretKeyBytes) {
        setSecretKey(new SecretKeySpec(secretKeyBytes, "AES"));
    }

    /**
     * 加密
     * 
     * @param input
     * @return
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] encode(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
        return cipher.doFinal(input);
    }

    /**
     * 解密
     * 
     * @param input
     * @return
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] decode(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
        return cipher.doFinal(input);
    }
}
