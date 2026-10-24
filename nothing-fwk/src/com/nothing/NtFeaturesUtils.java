/*
 * Copyright (C) 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.nothing;

import android.os.Build;
import android.os.SystemProperties;

import java.math.BigInteger;
import java.util.BitSet;

public class NtFeaturesUtils {

    // Read by name through reflection in Nothing Camera
    public static final int NTF_QCOM = 59;
    public static final int NTF_MTK = 60;
    public static final int NTF_PONG = 62;
    public static final int NTF_PACMAN = 63;
    public static final int NTF_TETRIS = 64;
    public static final int NTF_ASTEROIDS = 80;
    public static final int NTF_ASTEROIDS_PLUS = 86;
    public static final int NTF_JPN = 89;
    public static final int NTF_DRAGONITE = 110;
    public static final int NTF_BACKGROUND_RES_LIMIT = 115;

    private static final BitSet sFeatures;

    static {
        final String fullProp = SystemProperties.get("ro.vendor.nothing.feature.base", "0");
        final String productDiffProp = SystemProperties.get("ro.vendor.nothing.feature.diff.product." + Build.PRODUCT, "0");
        final String deviceDiffProp = SystemProperties.get("ro.vendor.nothing.feature.diff.device." + Build.DEVICE, "0");
        final String plusDiffProp = SystemProperties.get("ro.vendor.nothing.feature.diff.plus." + Build.DEVICE, "0");

        int bitsetSize = maxLength(replace(fullProp),replace(productDiffProp),replace(deviceDiffProp)) * 4;

        sFeatures = new BitSet(bitsetSize);

        base(new BigInteger(replace(fullProp), 16));
        change(new BigInteger(replace(productDiffProp), 16));
        change(new BigInteger(replace(deviceDiffProp), 16));
        setPro(plusDiffProp);
    }

    public static boolean isSupport(int... features) {
        for (int feature : features) {
            if (feature < 0 || feature >= sFeatures.length()) {
                return false;
            }
            if (!sFeatures.get(feature)) {
                return false;
            }
        }
        return true;
    }

    private static void base(BigInteger bi) {
        int index = 0;
        while (!bi.equals(BigInteger.ZERO)) {
            if (bi.testBit(0)) {
                sFeatures.set(index);
            }
            index++;
            bi = bi.shiftRight(1);
        }
    }

    private static void change(BigInteger bi) {
        int index = 0;
        while (!bi.equals(BigInteger.ZERO)) {
            if (bi.testBit(0)) {
                sFeatures.flip(index);
            }
            index++;
            bi = bi.shiftRight(1);
        }
    }

    private static String replace(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("0x", "").replace("L", "");
    }

    private static void setPro(String str) {
        if ("pro".equalsIgnoreCase(SystemProperties.get("ro.boot.pbid", "base"))) {
            change(new BigInteger(replace(str), 16));
        }
    }

    private static int maxLength(String... strs) {
        int max = 0;
        for (String s : strs) {
            if (s.length() > max) {
                max = s.length();
            }
        }
        return max;
    }
}
