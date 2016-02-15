/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * This file contains relicensed code from som Apache copyright of
 * Copyright (C) 2010, The Android Open Source Project
 * <p>
 * This file contains relicensed code from som Apache copyright of
 * Copyright (C) 2010, The Android Open Source Project
 * <p>
 * This file contains relicensed code from som Apache copyright of
 * Copyright (C) 2010, The Android Open Source Project
 * <p>
 * This file contains relicensed code from som Apache copyright of
 * Copyright (C) 2010, The Android Open Source Project
 */
/**
 * This file contains relicensed code from som Apache copyright of 
 * Copyright (C) 2010, The Android Open Source Project
 */

package com.crte.sipstackhome.service;

import android.os.PowerManager;

import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.HashSet;

public class SipWakeLock {
    private static final String TAG = "SipWakeLock";
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager.WakeLock mTimerWakeLock;
    private HashSet<Object> mHolders = new HashSet<Object>();

    public SipWakeLock(PowerManager powerManager) {
        mPowerManager = powerManager;
    }

    /**
     * 释放此锁重置所有持有人
     */
    public synchronized void reset() {
        mHolders.clear();
        release(null);
        if (mWakeLock != null) {
            while (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            LogUtils.v(TAG, "~~~ hard reset wakelock :: still held : " + mWakeLock.isHeld());
        }
    }

    public synchronized void acquire(long timeout) {
        if (mTimerWakeLock == null) {
            mTimerWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SipWakeLock.timer");
            mTimerWakeLock.setReferenceCounted(true);
        }
        mTimerWakeLock.acquire(timeout);
    }

    public synchronized void acquire(Object holder) {
        mHolders.add(holder);
        if (mWakeLock == null) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SipWakeLock");
        }
        if (!mWakeLock.isHeld()) mWakeLock.acquire();
        LogUtils.v(TAG, "acquire wakelock: holder count=" + mHolders.size());
    }

    public synchronized void release(Object holder) {
        mHolders.remove(holder);
        if ((mWakeLock != null) && mHolders.isEmpty() && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        LogUtils.v(TAG, "release wakelock: holder count=" + mHolders.size());
    }
}