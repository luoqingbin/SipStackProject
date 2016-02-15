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
 */

package com.crte.sipstackhome.pjsip;

import android.content.Intent;
import android.text.format.DateFormat;

import com.crte.sipstackhome.api.SipCallSession;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.exception.SameThreadException;

import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsua;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SimpleWavRecorderHandler implements IRecorderHandler {
    /**
     * 呼入呼出
     */
    final int way;
    final SipCallSession callInfo;
    private final int recorderId;
    /**
     * 文件路径
     */
    private final String recordingPath;

    public SimpleWavRecorderHandler(SipCallSession callInfo, File recordFolder, int way)
            throws SameThreadException, IOException {
        this.way = way;
        this.callInfo = callInfo;

        // 文件信息
        File targetFile = getRecordFile(recordFolder, callInfo.getRemoteContact(), way);
        if (targetFile == null) {
            throw new IOException("No target file possible");
        }
        recordingPath = targetFile.getAbsolutePath();
        pj_str_t file = pjsua.pj_str_copy(recordingPath);
        int[] rcId = new int[1];
        // 创建录音文件
        int status = pjsua.recorder_create(file, 0, (byte[]) null, 0, 0, rcId);
        if (status == pjsua.PJ_SUCCESS) {
            recorderId = rcId[0];
        } else {
            throw new IOException("Pjsip not able to write the file");
        }
    }

    /**
     * Get the file to record to for a given remote contact. This will
     * implicitly get the current date in file name.
     *
     * @param remoteContact The remote contact name
     * @return The file to store conversation
     */
    private File getRecordFile(File dir, String remoteContact, int way) {
        if (dir != null) {
            // The file name is only to have an unique identifier.
            // It should never be used to store datas as may change.
            // The app using the recording files should rely on the broadcast
            // and on callInfo instead that is reliable.
            String datePart = (String) DateFormat.format("yy-MM-dd_kkmmss", new Date());
            String remotePart = sanitizeForFile(remoteContact);
            String fileName = datePart + "_" + remotePart;
            if (way != (SipManager.BITMASK_ALL)) {
                fileName += ((way & SipManager.BITMASK_IN) == 0) ? "_out" : "_in";
            }
            File file = new File(dir.getAbsoluteFile() + File.separator + fileName + ".wav");
            return file;
        }
        return null;
    }

    private String sanitizeForFile(String remoteContact) {
        String fileName = remoteContact;
        fileName = fileName.replaceAll("[\\.\\\\<>:; \"\'\\*]", "_");
        return fileName;
    }

    @Override
    public void startRecording() {
        // TODO : treat connect errors ? is it useful? Should we fail gracefully
        int wavPort = pjsua.recorder_get_conf_port(recorderId);
        if ((way & SipManager.BITMASK_IN) == SipManager.BITMASK_IN) {
            int wavConfPort = callInfo.getConfPort();
            pjsua.conf_connect(wavConfPort, wavPort);
        }
        if ((way & SipManager.BITMASK_OUT) == SipManager.BITMASK_OUT) {
            pjsua.conf_connect(0, wavPort);
        }
    }

    @Override
    public void stopRecording() {
        pjsua.recorder_destroy(recorderId);
    }

    @Override
    public void fillBroadcastWithInfo(Intent it) {
        it.putExtra(SipManager.EXTRA_FILE_PATH, recordingPath);
        it.putExtra(SipManager.EXTRA_SIP_CALL_CALL_WAY, way);
    }

    /*
     * 开启录音
     * service.startRecording(call.getCallId(), SipManager.BITMASK_ALL);
     */

}
