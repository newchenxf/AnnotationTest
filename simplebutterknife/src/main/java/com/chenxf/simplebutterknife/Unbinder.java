package com.chenxf.simplebutterknife;

import android.support.annotation.UiThread;

/** An unbinder contract that will unbind views when called. */
public interface Unbinder {
  @UiThread
  void unbind();

  Unbinder EMPTY = null;
}