/*
 * Copyright (C) 2017-2019 Dremio Corporation. This file is confidential and private property.
 */

package com.dremio.throne.util;

public final class Util {

  public static long parseLongSafely(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private Util() {
  }

}


