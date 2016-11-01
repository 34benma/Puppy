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

package cn.wantedonline.puppy.httpserver.util.codec;

import java.nio.ByteOrder;

/**
 * 字节转换器
 *
 */
public abstract class ByteTranscoder {

    /**
     * 大端模式字节转换器
     * 
     * @author ZengDong
     */
    public static class BigEndianTranscoder extends ByteTranscoder {

        private BigEndianTranscoder() {
        }

        /**
         * 获得端模式
         */
        @Override
        public ByteOrder getEndian() {
            return ByteOrder.BIG_ENDIAN;
        }

        /**
         * 将array字节数组中从index开始的4位数看作Int型的四个字节的数，将其转化为10进制的整数
         */
        @Override
        public int getInt(byte[] array, int index) {
            return (array[index] & 0xff) << 24 | (array[index + 1] & 0xff) << 16 | (array[index + 2] & 0xff) << 8 | (array[index + 3] & 0xff) << 0;
        }

        /**
         * 将array字节数组中从index开始的8位数看作Long型的8个字节的数，将其转化为10进制的整数
         */
        @Override
        public long getLong(byte[] array, int index) {
            return ((long) array[index] & 0xff) << 56 | ((long) array[index + 1] & 0xff) << 48
                    | ((long) array[index + 2] & 0xff) << 40
                    | ((long) array[index + 3] & 0xff) << 32
                    | ((long) array[index + 4] & 0xff) << 24
                    | ((long) array[index + 5] & 0xff) << 16
                    | ((long) array[index + 6] & 0xff) << 8
                    | ((long) array[index + 7] & 0xff) << 0;
        }

        /**
         * 将array字节数组中从index开始的2位数看作Short型的两个字节的数，将其转化为10进制的整数
         */
        @Override
        public short getShort(byte[] array, int index) {
            return (short) (array[index] << 8 | array[index + 1] & 0xFF);
        }

        /**
         * 将array字节数组中从index开始的3位数看作无符号medium型的3个字节的数，将其转化为10进制的整数
         */
        @Override
        public int getUnsignedMedium(byte[] array, int index) {
            return (array[index] & 0xff) << 16 | (array[index + 1] & 0xff) << 8 | (array[index + 2] & 0xff) << 0;
        }

        /**
         * 将array中从index开始的4个元素设置为value对应的4个字节的数
         */
        @Override
        public void setInt(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 24);
            array[index + 1] = (byte) (value >>> 16);
            array[index + 2] = (byte) (value >>> 8);
            array[index + 3] = (byte) (value >>> 0);
        }

        /**
         * 将array中从index开始的8个元素设置为value对应的8个字节的数
         */
        @Override
        public void setLong(byte[] array, int index, long value) {
            array[index] = (byte) (value >>> 56);
            array[index + 1] = (byte) (value >>> 48);
            array[index + 2] = (byte) (value >>> 40);
            array[index + 3] = (byte) (value >>> 32);
            array[index + 4] = (byte) (value >>> 24);
            array[index + 5] = (byte) (value >>> 16);
            array[index + 6] = (byte) (value >>> 8);
            array[index + 7] = (byte) (value >>> 0);
        }

        /**
         * 将array中从index开始的3个元素设置为value对应的3个字节的数
         */
        @Override
        public void setMedium(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 16);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 0);
        }

        /**
         * 将array中从Index开始的2个元素设置为value对应的2个字节的数
         */
        @Override
        public void setShort(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 8);
            array[index + 1] = (byte) (value >>> 0);
        }
    }

    /**
     * 小端模式字节转换器
     * 
     * @author ZengDong
     */
    public static class LittleEndianTranscoder extends ByteTranscoder {

        private LittleEndianTranscoder() {
        }

        /**
         * 获得端模式
         */
        @Override
        public ByteOrder getEndian() {
            return ByteOrder.LITTLE_ENDIAN;
        }

        /**
         * 将array字节数组中从index开始的4位数看作Int型的四个字节的数，将其转化为10进制的整数
         */
        @Override
        public int getInt(byte[] array, int index) {
            return (array[index] & 0xff) << 0 | (array[index + 1] & 0xff) << 8 | (array[index + 2] & 0xff) << 16 | (array[index + 3] & 0xff) << 24;
        }

        /**
         * 将array字节数组中从index开始的8位数看作Long型的8个字节的数，将其转化为10进制的整数
         */
        @Override
        public long getLong(byte[] array, int index) {
            return ((long) array[index] & 0xff) << 0 | ((long) array[index + 1] & 0xff) << 8
                    | ((long) array[index + 2] & 0xff) << 16
                    | ((long) array[index + 3] & 0xff) << 24
                    | ((long) array[index + 4] & 0xff) << 32
                    | ((long) array[index + 5] & 0xff) << 40
                    | ((long) array[index + 6] & 0xff) << 48
                    | ((long) array[index + 7] & 0xff) << 56;
        }

        /**
         * 将array字节数组中从index开始的2位数看作Short型的两个字节的数，将其转化为10进制的整数
         */
        @Override
        public short getShort(byte[] array, int index) {
            return (short) (array[index] & 0xFF | array[index + 1] << 8);
        }

        /**
         * 将array字节数组中从index开始的3位数看作无符号medium型的3个字节的数，将其转化为10进制的整数
         */
        @Override
        public int getUnsignedMedium(byte[] array, int index) {
            return (array[index] & 0xff) << 0 | (array[index + 1] & 0xff) << 8 | (array[index + 2] & 0xff) << 16;
        }

        /**
         * 将array中从index开始的4个元素设置为value对应的4个字节的数
         */
        @Override
        public void setInt(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 16);
            array[index + 3] = (byte) (value >>> 24);
        }

        /**
         * 将array中从index开始的8个元素设置为value对应的8个字节的数
         */
        @Override
        public void setLong(byte[] array, int index, long value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 16);
            array[index + 3] = (byte) (value >>> 24);
            array[index + 4] = (byte) (value >>> 32);
            array[index + 5] = (byte) (value >>> 40);
            array[index + 6] = (byte) (value >>> 48);
            array[index + 7] = (byte) (value >>> 56);
        }

        /**
         * 将array中从index开始的3个元素设置为value对应的3个字节的数
         */
        @Override
        public void setMedium(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 16);
        }

        /**
         * 将array中从Index开始的2个元素设置为value对应的2个字节的数
         */
        @Override
        public void setShort(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
        }
    }

    /**
     * 大端模式的端转换器引用
     */
    public static final BigEndianTranscoder bigEndianTranscoder = new BigEndianTranscoder();
    /**
     * 小端模式的端转换器引用
     */
    public static final LittleEndianTranscoder littleEndianTranscoder = new LittleEndianTranscoder();

    /**
     * 根据端模式来获得响应的转换器
     * 
     * @param endian
     * @return
     */
    public static ByteTranscoder getInstance(ByteOrder endian) {
        return endian == ByteOrder.BIG_ENDIAN ? bigEndianTranscoder : littleEndianTranscoder;
    }

    /**
     * 将array数组按照char类型解码为整数
     * 
     * @param array
     * @return
     */
    public int decodeChar(byte[] array) {
        return (char) getShort(array, 0);
    }

    /**
     * 将array数组按照double类型解码为浮点数
     * 
     * @param array
     * @return
     */
    public double decodeDouble(byte[] array) {
        return getDouble(array, 0);
    }

    /**
     * 将array数组按照float类型解码为浮点数
     * 
     * @param array
     * @return
     */
    public float decodeFloat(byte[] array) {
        return getFloat(array, 0);
    }

    /**
     * 将array数组按照int类型解码为int型整数
     * 
     * @param array
     * @return
     */
    public int decodeInt(byte[] array) {
        return getInt(array, 0);
    }

    /**
     * 将array数组按照long类型解码为long型整数
     * 
     * @param array
     * @return
     */
    public long decodeLong(byte[] array) {
        return getLong(array, 0);
    }

    /**
     * 将array数组按照Medium类型解码为int型整数
     * 
     * @param array
     * @return
     */
    public int decodeMedium(byte[] array) {
        return getMedium(array, 0);
    }

    /**
     * 将array数组按照short类型解码为short型整数
     * 
     * @param array
     * @return
     */
    public short decodeShort(byte[] array) {
        return getShort(array, 0);
    }

    /**
     * 将byte转化为无符号的byte
     * 
     * @param b
     * @return
     */
    public short decodeUnsignedByte(byte b) {
        return (short) (b & 0xFF);
    }

    /**
     * 将array看做是有符号的数，将其转化为long
     * 
     * @param array
     * @return
     */
    public long decodeUnsignedInt(byte[] array) {
        return getUnsignedInt(array, 0);
    }

    /**
     * 将array看做是有符号的数，将其转化为Medium
     * 
     * @param array
     * @return
     */
    public int decodeUnsignedMedium(byte[] array) {
        return getUnsignedMedium(array, 0);
    }

    /**
     * 将array看做是有符号的数，将其转化为short
     * 
     * @param array
     * @return
     */
    public int decodeUnsignedShort(byte[] array) {
        return getUnsignedShort(array, 0);
    }

    /**
     * 将char类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeChar(int value) {
        return encodeShort(value);
    }

    /**
     * 将double类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeDouble(double value) {
        byte[] array = new byte[8];
        setDouble(array, 0, value);
        return array;
    }

    /**
     * 将float类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeFloat(float value) {
        byte[] array = new byte[4];
        setFloat(array, 0, value);
        return array;
    }

    /**
     * 将int类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeInt(int value) {
        byte[] array = new byte[4];
        setInt(array, 0, value);
        return array;
    }

    /**
     * 将long类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeLong(long value) {
        byte[] array = new byte[8];
        setLong(array, 0, value);
        return array;
    }

    /**
     * 将Medium类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeMedium(int value) {
        byte[] array = new byte[3];
        setMedium(array, 0, value);
        return array;
    }

    /**
     * 将short类型的value转化为字节数组
     * 
     * @param value
     * @return
     */
    public byte[] encodeShort(int value) {
        byte[] array = new byte[2];
        setShort(array, 0, value);
        return array;
    }

    /**
     * 获得array数组中的地index位
     * 
     * @param array
     * @param index
     * @return
     */
    public byte getByte(byte[] array, int index) {
        return array[index];
    }

    /**
     * 获得array数组的第index位字符
     * 
     * @param array
     * @param index
     * @return
     */
    public char getChar(byte[] array, int index) {
        return (char) getShort(array, index);
    }

    /**
     * 获得array数组的第index位数
     * 
     * @param array
     * @param index
     * @return
     */
    public double getDouble(byte[] array, int index) {
        return Double.longBitsToDouble(getLong(array, index));
    }

    /**
     * 获得端模式
     * 
     * @return
     */
    public abstract ByteOrder getEndian();

    /**
     * 获得第array的第index位数，转化为float型
     * 
     * @param array
     * @param index
     * @return
     */
    public float getFloat(byte[] array, int index) {
        return Float.intBitsToFloat(getInt(array, index));
    }

    /**
     * 将array字节数组中从index开始的4位数看作Int型的四个字节的数，将其转化为10进制的整数
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract int getInt(byte[] array, int index);

    /**
     * 将array字节数组中从index开始的8位数看作long型的8个字节的数，将其转化为10进制的整数
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract long getLong(byte[] array, int index);

    /**
     * 将array字节数组中从index开始的3位数看作Medium型的3个字节的数，将其转化为10进制的整数
     * 
     * @param array
     * @param index
     * @return
     */
    public int getMedium(byte[] array, int index) {
        int value = getUnsignedMedium(array, index);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * 将array字节数组中从index开始的2位数看作short型的2个字节的数，将其转化为10进制的整数
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract short getShort(byte[] array, int index);

    /**
     * 将array字节数组中从index开始的1位数看作byte型的1个字节的数，将其转化为10进制的正整数
     * 
     * @param array
     * @param index
     * @return
     */
    public short getUnsignedByte(byte[] array, int index) {
        return (short) (getByte(array, index) & 0xFF);
    }

    /**
     * 将array字节数组中从index开始的4位数看作Int型的4个字节的数，将其转化为10进制的正整数
     * 
     * @param array
     * @param index
     * @return
     */
    public long getUnsignedInt(byte[] array, int index) {
        return getInt(array, index) & 0xFFFFFFFFL;
    }

    /**
     * 将array字节数组中从index开始的3位数看作Medium型的3个字节的数，将其转化为10进制的正整数
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract int getUnsignedMedium(byte[] array, int index);

    /**
     * 将array字节数组中从index开始的2位数看作short型的2个字节的数，将其转化为10进制的正整数
     * 
     * @param array
     * @param index
     * @return
     */
    public int getUnsignedShort(byte[] array, int index) {
        return getShort(array, index) & 0xFFFF;
    }

    /**
     * 将array中从index开始的2个元素设置为value对应的2个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public void setChar(byte[] array, int index, int value) {
        setShort(array, index, value);
    }

    /**
     * 将array中从index开始的8个元素设置为value对应的8个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public void setDouble(byte[] array, int index, double value) {
        setLong(array, index, Double.doubleToRawLongBits(value));
    }

    /**
     * 将array中从index开始的4个元素设置为value对应的4个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public void setFloat(byte[] array, int index, float value) {
        setInt(array, index, Float.floatToRawIntBits(value));
    }

    /**
     * 将array中从index开始的4个元素设置为value对应的4个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setInt(byte[] array, int index, int value);

    /**
     * 将array中从index开始的8个元素设置为value对应的8个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setLong(byte[] array, int index, long value);

    /**
     * 将array中从index开始的3个元素设置为value对应的3个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setMedium(byte[] array, int index, int value);

    /**
     * 将array中从index开始的2个元素设置为value对应的2个字节的数
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setShort(byte[] array, int index, int value);
}
