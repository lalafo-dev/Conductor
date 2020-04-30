Change Log
==========

Version 1.0.0 *(2020-04-30)*
----------------------------

This version of Conductor has a difference from the original library

 * Breaking changes:
   -  Remove RxLifecycle and Autodispose support.
   -  Remove the RestoreViewOnCreateController and merge the functionallity.
   -  Remove the TransitionChangeHandler from main lib and put to module.
   -  Remove option menu support.
   -  Replace android.app.Fragment on androidx.fragment.app.Fragment.

 * New:
   - Implement ControllerFactory.
   - Add Dagger2 Codegen (based on AssistedInject) for generate ControllerFactory.
   - Put DialogController into main library.
   - Add Glide support.
