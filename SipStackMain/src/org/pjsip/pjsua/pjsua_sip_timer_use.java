/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

public enum pjsua_sip_timer_use {
  PJSUA_SIP_TIMER_INACTIVE,
  PJSUA_SIP_TIMER_OPTIONAL,
  PJSUA_SIP_TIMER_REQUIRED,
  PJSUA_SIP_TIMER_ALWAYS;

  public final int swigValue() {
    return swigValue;
  }

  public static pjsua_sip_timer_use swigToEnum(int swigValue) {
    pjsua_sip_timer_use[] swigValues = pjsua_sip_timer_use.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (pjsua_sip_timer_use swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + pjsua_sip_timer_use.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private pjsua_sip_timer_use() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private pjsua_sip_timer_use(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private pjsua_sip_timer_use(pjsua_sip_timer_use swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

