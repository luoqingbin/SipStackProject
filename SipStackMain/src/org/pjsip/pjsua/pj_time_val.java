/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

public class pj_time_val {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected pj_time_val(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(pj_time_val obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsuaJNI.delete_pj_time_val(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setSec(int value) {
    pjsuaJNI.pj_time_val_sec_set(swigCPtr, this, value);
  }

  public int getSec() {
    return pjsuaJNI.pj_time_val_sec_get(swigCPtr, this);
  }

  public void setMsec(int value) {
    pjsuaJNI.pj_time_val_msec_set(swigCPtr, this, value);
  }

  public int getMsec() {
    return pjsuaJNI.pj_time_val_msec_get(swigCPtr, this);
  }

  public pj_time_val() {
    this(pjsuaJNI.new_pj_time_val(), true);
  }

}
