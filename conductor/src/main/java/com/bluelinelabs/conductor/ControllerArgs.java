package com.bluelinelabs.conductor;

import android.os.Parcelable;

/**
 * Interface used to bundle controller params without necessity
 * to wrap them in outer {@link android.os.Bundle} object.
 *
 * {@link ControllerArgs} is stored inside the {@link Controller} saved state
 * controller and it is private inside of Controller object,
 * so you should manage access to your {@link ControllerArgs} options by yourself.
 *
 * Also, {@link ControllerArgs} is having protected {@link Controller} constructor,
 * so Conductor do not restore the controller with reflection just a Controller
 * Bundle arguments. To manage {@link ControllerArgs} instantiation take a look on
 * {@link ControllerFactory}, define your own on project level and register it with
 * {@link Conductor#setControllerFactory(ControllerFactory)}
 *
 * @author Artyom Dorosh (artyom.dorosh@outlook.com)
 * @see android.os.Parcelable
 * @see com.bluelinelabs.conductor.Controller
 * @see com.bluelinelabs.conductor.Conductor#setControllerFactory(ControllerFactory)
 * @since 2.0
 */
public interface ControllerArgs extends Parcelable {}
