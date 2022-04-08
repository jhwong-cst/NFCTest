package com.joonhuiwong.nfctest;

public class CommonUtils {

    public static String[] splitString(String text, int length) {
        String regex = "(.{" + length + "})";
        String newStr = text.replaceAll(regex, "$1|");
        return newStr.split("\\|");
    }

    public static byte[] stringToHex(String str) {
        byte[] by = new byte[4]; // 4 because the size of each write is 4 bytes
        byte[] temp = str.getBytes();
        int length = str.length();
        for (int i = 0; i < length / 2; i++) {
            by[i] = string2Hex(temp[2 * i], temp[2 * i + 1]);
        }
        return by;
    }

    public static byte string2Hex(byte src0, byte src1) {
        byte _bo = Byte.decode("0x" + new String(new byte[]{src0}));
        _bo = (byte) (_bo << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}));
        return (byte) (_bo ^ _b1);
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    public static long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (byte aByte : bytes) {
            long value = aByte & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    public static long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

}
