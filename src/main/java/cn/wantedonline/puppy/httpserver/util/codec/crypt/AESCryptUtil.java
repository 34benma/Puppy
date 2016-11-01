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

import cn.wantedonline.puppy.httpserver.util.StringHelper;
import cn.wantedonline.puppy.httpserver.util.codec.DigestUtils;
import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;


public class AESCryptUtil {

    /**
     * AES加密类池
     */
    public static final ThreadLocal<AESCrypt> aesCryptFactory = new ThreadLocal<AESCrypt>() {

        @Override
        protected synchronized AESCrypt initialValue() {
            try {
                AESCrypt aes = new AESCrypt();
                return aes;
            } catch (Exception e) {
                log.error("", e);
            }
            return null;
        }
    };
    private static final Logger log = Log.getLogger();

    /**
     * @param aesSecretKey
     * @param needDecodeContent
     * @return
     * @throws Exception
     */
    public static byte[] aesDecode(byte[] aesSecretKey, byte[] needDecodeContent) throws Exception {
        AESCrypt aes = aesCryptFactory.get();
        aes.setSecretKey(aesSecretKey);
        return aes.decode(needDecodeContent);
    }

    /**
     * @param aesSecretKey
     * @param needEncodeContent
     * @return
     * @throws Exception
     */
    public static byte[] aesEncode(byte[] aesSecretKey, byte[] needEncodeContent) throws Exception {
        AESCrypt aes = aesCryptFactory.get();
        aes.setSecretKey(aesSecretKey);
        return aes.encode(needEncodeContent);
    }

    /**
     * 返回一个AESCrypt,其使用needMd5ToSecretKey的md5后的byte[]来作为secretKey
     * 
     * @param needMd5ToSecretKey
     * @return
     */
    public static AESCrypt getAesByMd5(byte[] needMd5ToSecretKey) {
        AESCrypt aes = aesCryptFactory.get();
        aes.setSecretKey(getSecretKeyByMd5(needMd5ToSecretKey));
        return aes;
    }

    /**
     * 返回一个AESCrypt,其使用needMd5ToSecretKey的字符串拼接后md5返回的byte[]来作为secretKey
     * 
     * @param needMd5ToSecretKey
     * @return
     */
    public static AESCrypt getAesByMd5(Object... needMd5ToSecretKey) {
        AESCrypt aes = aesCryptFactory.get();
        aes.setSecretKey(getSecretKeyByMd5(needMd5ToSecretKey));
        return aes;
    }

    /**
     * md5(needMd5ToSecretKey),主要用于AESCrypt的secretKey
     * 
     * @param needMd5ToSecretKey
     * @return
     */
    public static byte[] getSecretKeyByMd5(byte[] needMd5ToSecretKey) {
        return DigestUtils.md5(needMd5ToSecretKey);
    }

    /**
     * 字符串拼接 needMd5ToSecretKey这些值,然后md5,主要用于AESCrypt的secretKey
     * 
     * @param needMd5ToSecretKey
     * @return
     */
    public static byte[] getSecretKeyByMd5(Object... needMd5ToSecretKey) {
        return DigestUtils.md5(StringHelper.concate(needMd5ToSecretKey));
    }
}
