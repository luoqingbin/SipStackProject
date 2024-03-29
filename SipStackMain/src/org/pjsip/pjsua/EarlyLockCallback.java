/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

public class EarlyLockCallback {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected EarlyLockCallback(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(EarlyLockCallback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsuaJNI.delete_EarlyLockCallback(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    pjsuaJNI.EarlyLockCallback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    pjsuaJNI.EarlyLockCallback_change_ownership(this, swigCPtr, true);
  }

  public void on_create_early_lock() {
    if (getClass() == EarlyLockCallback.class) pjsuaJNI.EarlyLockCallback_on_create_early_lock(swigCPtr, this); else pjsuaJNI.EarlyLockCallback_on_create_early_lockSwigExplicitEarlyLockCallback(swigCPtr, this);
  }

  public EarlyLockCallback() {
    this(pjsuaJNI.new_EarlyLockCallback(), true);
    pjsuaJNI.EarlyLockCallback_director_connect(this, swigCPtr, swigCMemOwn, false);
  }

}

