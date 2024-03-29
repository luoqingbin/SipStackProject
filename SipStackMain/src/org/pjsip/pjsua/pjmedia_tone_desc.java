/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

public class pjmedia_tone_desc {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected pjmedia_tone_desc(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(pjmedia_tone_desc obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsuaJNI.delete_pjmedia_tone_desc(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected static long[] cArrayUnwrap(pjmedia_tone_desc[] arrayWrapper) {
      long[] cArray = new long[arrayWrapper.length];
      for (int i=0; i<arrayWrapper.length; i++)
        cArray[i] = pjmedia_tone_desc.getCPtr(arrayWrapper[i]);
      return cArray;
  }

  protected static pjmedia_tone_desc[] cArrayWrap(long[] cArray, boolean cMemoryOwn) {
    pjmedia_tone_desc[] arrayWrapper = new pjmedia_tone_desc[cArray.length];
    for (int i=0; i<cArray.length; i++)
      arrayWrapper[i] = new pjmedia_tone_desc(cArray[i], cMemoryOwn);
    return arrayWrapper;
  }

  public void setFreq1(short value) {
    pjsuaJNI.pjmedia_tone_desc_freq1_set(swigCPtr, this, value);
  }

  public short getFreq1() {
    return pjsuaJNI.pjmedia_tone_desc_freq1_get(swigCPtr, this);
  }

  public void setFreq2(short value) {
    pjsuaJNI.pjmedia_tone_desc_freq2_set(swigCPtr, this, value);
  }

  public short getFreq2() {
    return pjsuaJNI.pjmedia_tone_desc_freq2_get(swigCPtr, this);
  }

  public void setOn_msec(short value) {
    pjsuaJNI.pjmedia_tone_desc_on_msec_set(swigCPtr, this, value);
  }

  public short getOn_msec() {
    return pjsuaJNI.pjmedia_tone_desc_on_msec_get(swigCPtr, this);
  }

  public void setOff_msec(short value) {
    pjsuaJNI.pjmedia_tone_desc_off_msec_set(swigCPtr, this, value);
  }

  public short getOff_msec() {
    return pjsuaJNI.pjmedia_tone_desc_off_msec_get(swigCPtr, this);
  }

  public void setVolume(short value) {
    pjsuaJNI.pjmedia_tone_desc_volume_set(swigCPtr, this, value);
  }

  public short getVolume() {
    return pjsuaJNI.pjmedia_tone_desc_volume_get(swigCPtr, this);
  }

  public void setFlags(short value) {
    pjsuaJNI.pjmedia_tone_desc_flags_set(swigCPtr, this, value);
  }

  public short getFlags() {
    return pjsuaJNI.pjmedia_tone_desc_flags_get(swigCPtr, this);
  }

  public pjmedia_tone_desc() {
    this(pjsuaJNI.new_pjmedia_tone_desc(), true);
  }

}
