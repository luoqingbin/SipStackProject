/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

public class pjsua_codec_info {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected pjsua_codec_info(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(pjsua_codec_info obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsuaJNI.delete_pjsua_codec_info(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setCodec_id(pj_str_t value) {
    pjsuaJNI.pjsua_codec_info_codec_id_set(swigCPtr, this, pj_str_t.getCPtr(value), value);
  }

  public pj_str_t getCodec_id() {
    long cPtr = pjsuaJNI.pjsua_codec_info_codec_id_get(swigCPtr, this);
    return (cPtr == 0) ? null : new pj_str_t(cPtr, false);
  }

  public void setPriority(short value) {
    pjsuaJNI.pjsua_codec_info_priority_set(swigCPtr, this, value);
  }

  public short getPriority() {
    return pjsuaJNI.pjsua_codec_info_priority_get(swigCPtr, this);
  }

  public void setDesc(pj_str_t value) {
    pjsuaJNI.pjsua_codec_info_desc_set(swigCPtr, this, pj_str_t.getCPtr(value), value);
  }

  public pj_str_t getDesc() {
    long cPtr = pjsuaJNI.pjsua_codec_info_desc_get(swigCPtr, this);
    return (cPtr == 0) ? null : new pj_str_t(cPtr, false);
  }

  public void setBuf_(String value) {
    pjsuaJNI.pjsua_codec_info_buf__set(swigCPtr, this, value);
  }

  public String getBuf_() {
    return pjsuaJNI.pjsua_codec_info_buf__get(swigCPtr, this);
  }

  public pjsua_codec_info() {
    this(pjsuaJNI.new_pjsua_codec_info(), true);
  }

}
