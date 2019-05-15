package com.chenxf.simplebutterknife;

import android.support.annotation.IdRes;
import android.view.View;


@SuppressWarnings("WeakerAccess") // Used by generated code.
public final class Utils {

  public static <T> T findOptionalViewAsType(View source, @IdRes int id, String who,
      Class<T> cls) {
    View view = source.findViewById(id);
    return castView(view, id, who, cls);
  }


  public static <T> T castView(View view, @IdRes int id, String who, Class<T> cls) {
    try {
      return cls.cast(view);
    } catch (ClassCastException e) {
      String name = getResourceEntryName(view, id);
      throw new IllegalStateException("View '"
          + name
          + "' with ID "
          + id
          + " for "
          + who
          + " was of the wrong type. See cause for more info.", e);
    }
  }


  private static String getResourceEntryName(View view, @IdRes int id) {
    if (view.isInEditMode()) {
      return "<unavailable while editing>";
    }
    return view.getContext().getResources().getResourceEntryName(id);
  }

  private Utils() {
    throw new AssertionError("No instances.");
  }
}