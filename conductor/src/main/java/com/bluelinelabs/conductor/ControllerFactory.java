package com.bluelinelabs.conductor;

import android.os.Bundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ControllerFactory {
  private static final HashMap<String, Class<?>> classMap = new HashMap<>();

  @NonNull
  private static Class<?> loadClass(@NonNull ClassLoader classLoader, @NonNull String className) throws ClassNotFoundException {
    Class<?> clazz = classMap.get(className);
    if (clazz == null) {
      // Class not found in the cache, see if it's real, and try to add it
      clazz = Class.forName(className, false, classLoader);
      classMap.put(className, clazz);
    }
    return clazz;
  }

  static boolean isControllerClass(@NonNull ClassLoader classLoader, @NonNull String className) {
    try {
      Class<?> clazz = loadClass(classLoader, className);
      return Controller.class.isAssignableFrom(clazz);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public static Class<? extends Controller> loadControllerClass(@NonNull ClassLoader classLoader, @NonNull String className) {
    try {
      Class<?> clazz = loadClass(classLoader, className);
      return (Class<? extends Controller>) clazz;
    } catch (ClassNotFoundException e) {
      throw new Controller.InstantiationException("Unable to instantiate controller " + className
        + ": make sure class name exists", e);
    } catch (ClassCastException e) {
      throw new Controller.InstantiationException("Unable to instantiate controller " + className
        + ": make sure class is a valid subclass of Controller", e);
    }
  }

  @Nullable
  private static Constructor getDefaultConstructor(@NonNull Constructor[] constructors) {
    for (Constructor constructor : constructors) {
      if (constructor.getParameterTypes().length == 0) {
        return constructor;
      }
    }
    return null;
  }

  @Nullable
  private static Constructor getBundleConstructor(@NonNull Constructor[] constructors) {
    for (Constructor constructor : constructors) {
      if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0] == Bundle.class) {
        return constructor;
      }
    }
    return null;
  }

  @NonNull
  public Controller newInstance(@NonNull ClassLoader classLoader, @NonNull String className, @Nullable Object args) {
    Class<? extends Controller> cls = loadControllerClass(classLoader, className);

    Constructor[] constructors = cls.getConstructors();
    Constructor bundleConstructor = getBundleConstructor(constructors);

    Controller controller;

    Bundle bundle;
    if (args == null) {
      bundle = null;
    } else if (args instanceof Bundle) {
      bundle = (Bundle) args;
    } else {
      final IllegalArgumentException e = new IllegalArgumentException("Expected non null Bundle but got '" + args +
        "' object instead.");
      throw new Controller.InstantiationException("Unable to instantiate controller " + className
        + ": instantiation arguments are illegal", e);
    }

    try {
      if (bundleConstructor != null) {
        controller = (Controller) bundleConstructor.newInstance(bundle);
      } else {
        //noinspection ConstantConditions
        controller = (Controller) getDefaultConstructor(constructors).newInstance();

        // Restore the args that existed before the last process death
        if (args != null) {
          controller.args.putAll(bundle);
        }
      }
    } catch (IllegalAccessException e) {
      throw new Controller.InstantiationException("Unable to instantiate controller " + className
        + ": make sure class name exists, is public, and has an"
        + " empty constructor that is public", e);
    } catch (InstantiationException e) {
      throw new Controller.InstantiationException("Unable to instantiate controller " + className
        + ": make sure class name exists, is public, and has an"
        + " empty constructor that is public", e);
    } catch (InvocationTargetException e) {
      throw new Controller.InstantiationException("Unable to instantiate controller " + className
        + ": calling Controller constructor caused an exception", e);
    }

    return controller;
  }

}
