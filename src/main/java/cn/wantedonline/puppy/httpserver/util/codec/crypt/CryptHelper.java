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

import java.util.Random;


public class CryptHelper {

    /**
     * 随机生成指定长度的16进制byte字符串
     * 
     * @param len byte长度
     */
    public static String randomByteString(int len) {
        int length = len << 1;
        char[] arr = new char[length];
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            arr[i] = SecretHex.DIGITS_NORMAL[rand.nextInt(16)];
        }
        return new String(arr);
    }

}
