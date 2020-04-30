[![Travis CI](https://travis-ci.org/lalafo-dev/Conductor.svg?branch=develop)](https://travis-ci.org/lalafo-dev/Conductor)  [ ![Download](https://api.bintray.com/packages/lalafo-dev/Conductor/com.lalafo.conductor/images/download.svg) ](https://bintray.com/lalafo-dev/Conductor/com.lalafo.conductor/_latestVersion)

# Conductor

A small, yet full-featured framework that allows building View-based Android applications. Conductor provides a light-weight wrapper around standard Android Views that does just about everything you'd want:

|           |  Conductor  |
|-----------|-------------|
:tada: | Easy integration
:point_up: | Single Activity apps without using Fragments
:recycle: | Simple but powerful lifecycle management
:train: | Navigation and backstack handling
:twisted_rightwards_arrows: | Beautiful transitions between views
:floppy_disk: | State persistence
:phone: | Callbacks for onActivityResult, onRequestPermissionsResult, etc
:european_post_office: | MVP / MVVM / VIPER / MVC ready

Conductor is architecture-agnostic and does not try to force any design decisions on the developer. We here at Lalafo tend to use  MVP, but it would work equally well with standard MVVM, MVC or whatever else you want to throw at it.

## Installation

```gradle
def conductorVersion = '1.0.0'

implementation "com.lalafo.conductor:conductor:$conductorVersion"

// If you want Conductor Dagger2 codegen support add
// Annotations:
implementation "com.lalafo.conductor:conductor-codegen-annotations:$conductorVersion"
// Annotations processor:
annotationProcessor "com.lalafo.conductor:conductor-codegen-compiler:$conductorVersion"
// Or in case of using Kotlin
kapt "com.lalafo.conductor:conductor-codegen-compiler:$conductorVersion"

// If you want Controllers that are Lifecycle-aware (architecture components):
implementation "com.lalafo.conductor:conductor-archlifecycle:$conductorVersion"

// If you want Glide support Controller lifecycle:
implementation "com.lalafo.conductor:conductor-glide:$conductorVersion"
```

**SNAPSHOT builds**  
Just use `1.0.0-SNAPSHOT` as your version number in any of the dependencies above and add the url to the snapshot repository:

```gradle
allprojects {
  repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
  }
}
```

## Components to Know

|             |  Conductor Components |
------|------------------------------
__Controller__ | The Controller is the View wrapper that will give you all of your lifecycle management features. Think of it as a lighter-weight and more predictable Fragment alternative with an easier to manage lifecycle.
__Router__ | A Router implements navigation and backstack handling for Controllers. Router objects are attached to Activity/containing ViewGroup pairs. Routers do not directly render or push Views to the container ViewGroup, but instead defer this responsibility to the ControllerChangeHandler specified in a given transaction.
__ControllerChangeHandler__ | ControllerChangeHandlers are responsible for swapping the View for one Controller to the View of another. They can be useful for performing animations and transitions between Controllers. Several default ControllerChangeHandlers are included.
__RouterTransaction__ | Transactions are used to define data about adding Controllers. RouterTransactions are used to push a Controller to a Router with specified ControllerChangeHandlers, while ChildControllerTransactions are used to add child Controllers.
__ControllerFactory__ | Controller factory is used to instance the controllers with your own logic. It could be useful if you want to pass custom parameters on Controller instantiation during app running normally or after process death.

## Getting Started

### Minimal Activity implementation

```java
public class MainActivity extends Activity {

    private Router router;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ViewGroup container = (ViewGroup) findViewById(R.id.controller_container);

        router = Conductor.attachRouter(this, container, savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new HomeController()));
        }
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

}
```

### Minimal Controller implementation

```java
public class HomeController extends Controller {

    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflater.inflate(R.layout.controller_home, container, false);
        ((TextView) view.findViewById(R.id.tv_title)).setText("Hello World");
        return view;
    }

}
```

### Sample Project

[Demo app](https://github.com/lalafo-dev/Conductor/tree/master/demo) - Shows how to use all basic and most advanced functions of Conductor.

### Controller Lifecycle

The lifecycle of a Controller is significantly simpler to understand than that of a Fragment. A lifecycle diagram is shown below:

![Controller Lifecycle](docs/Controller%20Lifecycle.jpg)

## Advanced Topics
### ControllerFactory
Controller factory could be useful if you want to instance the Controller on your own. Just like Fragments, by default Conductor require you to have public empty constructor or public constructor with a single `Bundle args` argument, so it will be able to instantiate the given Controller by calling one of these constructors via reflection. This means by default you can't just pass anything into your own Controller constructor.

This is the situation when ControllerFactory could be handy. First, create your own factory:
```java
public class AppFactory extends ControllerFactory {

  @NonNull @Override public Controller newInstance(@NonNull ClassLoader classLoader, @NonNull String className, @Nullable Object args) {
    Class<? extends Controller>  klass = loadControllerClass(classLoader, className);
    if (klass == HomeController.class) {
      return new HomeController("your controller creation", "goes here", 42, args);
    } else {
      return super.newInstance(classLoader, className, args);
    }
  }
}
```
Then set it to Conductor before starting any transaction.
```java
Router router = Conductor.attachRouter(MainActivity.this, container, savedInstanceState);
Conductor.setControllerFactory(new AppFactory());
```
Finally, create the transaction:
```java
if (!router.hasRootController()) {
    // Create transaction with class so the Conductor will use 
    // your Controller to instantiate it.
    router.setRoot(RouterTransaction.with(HomeController.class, null));
} 
```
As you saw you should use  another method to create the transaction:
```java
RouterTransaction.with(@NonNull Class<? extends Controller> controllerClass, @Nullable Object arguments);
```
The arguments passed to `RouterTransaction.with(...)` are the exact arguments that will be passed to your `ControllerFactory` here:
```java
Controller newInstance(@NonNull ClassLoader classLoader, @NonNull String className, @Nullable Object args) { ... }
```
So you will be able to pass the arguments manually while creating the new transaction.  
**NOTE**: the passed arguments are not saved in the `ControllerTransaction`. They do not survive after the process death. Keep this gotcha in mind, because that's when `conductor-codegen` is useful.

### Dagger2 Codegen
If you are using [Dagger2](https://github.com/google/dagger) you are probably using Daggers `MemebersInjector` pattern, that's looks like this in Java:
```java
public class HomeController extends Controller {
  
  @Inject
  public String injectedData;
  
  // Code...
}
```
and even worse in Kotlin:
```kotlin
class HomeController: Controller() {

    @Inject
    lateinit var injectedData: String
    
    // Code...
}
```
and this still requires to inject Controllers manually using components:
```java
// Inject things to controller
Component component = ...; // Getting your dagger component
component.inject(this);
// And only after you could use injected objects
```
Ideally we want to inject things using the constructor injection. This gives us some goodies:
* All the data is present in the object at any moment
* No need to care about manual injection
* Clean looking class without public and late initialization or nullable fields.

As you have `ControllerFactory` you could it in 3 ways.
1. Write your own `ControllerFactory` implementation and maintain it, using `@Inject` on Controllers constructors.
2. Use [`AssistedInject`](https://github.com/square/AssistedInject) to pass the data to Controllers mixing injections and params data. It's a good approach but you still will got to manage the saving of passed controller params over constructor.
3. Use `conductor-codegen` package from our repo in couple with `ControllerArgs`.

`conductor-codegen` is highly inspired with [AssistedInject](https://github.com/square/AssistedInject) from Square. So if you used AssistedInject before you will probably get it right fast.
#### Case 1: Controller constructor injection without arguments
**1. Create controller with Injection Constructor**
```kotlin
class HomeController @InjectController constructor(
    private val injectedData: String
): Controller() {
    // More code ...
}
```
**2. Create your `AppControllerFactory`**
```kotlin
@ConductorFactory
open class AppControllerFactory: ControllerFactory()
```
**NOTE**: Make sure to make it non final.

**3. Declare you Dagger2 module for `conductor-codegen` bindings**
```java
@ConductorBindingModule
@Module(includes = ConductorModule_ConductorBindingModule.class)
public abstract class ConductorModule {}
```
**NOTE**: Make sure to add `includes = YOURCLASSNAME_ConductorBindingModule.class` to `@Module` so the Dagger would be able to pick up all the bindings.

**4. Register your `AppControllerFactory` to Conductor in you Activity or App class**
```kotlin
class MainActivity: AppCompatActivity() {
    @Inject lateinit var appControllerFactory: AppControllerFactory
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inject all the objects 
        // ...
        Conductor.setControllerFactory(appControllerFactory)
        // Execute yout transactions safely after that
    }
}
```
**5. All done!** Just make sure to create transactions using `RouterTransaction.with(ControllerClassName.class, null)` method.
#### Case 2: Controller constructor injection with `ControllerArgs`
It's nice to have constructor injection. You know what is nicer? Having your arguments in the constructor too. Now you are bundling your arguments to `Bundle args` so they survive the Controller recreation. But this is kinda messy, and you probably already know why. So we decided to move further and to add `ControllerArgs`.

`ControllerArgs` is an interface, that extends `Parcelable`. Controller has public constructor, that takes your implementation of `ControllerArgs` and stores it internally, so the args are saved between Controller recreation.

And don't be scared about the `Parcelable` and all this stuff you have to write to make class a Parcelable one. If you are using Kotlin, you already have [`@Parcelize`](https://kotlinlang.org/docs/tutorials/android-plugin.html#parcelable-implementations-generator), or if you stick to Java, you could use just as useful [AutoValue: Parcel](https://github.com/rharter/auto-value-parcel) extensions.

**1. Create your args**
```kotlin
@Parcelize
data class StringArgs(val value: String): ControllerArgs
```
**2. Add them to the Controller injection constructor**
```kotlin
class HomeController @InjectController constructor(
    @ControllerBundle private val args: StringArgs,
    private val injectedMeme: Thermosiphon
) : Controller(args) {
    // Blah blah blah ...
}
```
**3. Make sure to setup factory and module**  
Just make sure to execute all steps in **Case 1** from **2** to **4**.

**4. Put arguments to your transaction**
```
RouterTransaction.with(HomeController.class, new StringArgs("Subscribe for r/mAndroidDev on Reddit!");
```
**5. Done!** Now you have **both** constructor injection and constructor arguments!

#### Notes
1. **Only one or none** `ControllerArgs` implementing classes are allowed in the controller injection constructor for now.
2. `@ControllerBundle` is an optional annotation. The codegen will detect the your "controller bundle" without it, but it's bad for the code style. Just add it so you recognize them fast in the constuctor.
3. You can add `@InjectController.Factory` to your Controller, if you want to in analogue to `AssistedInject` library. But it's optional, so you better add them if you really need one.
4. Conductor already bundled with some lint rules for the `codegen` that are experimental and not really stable or tested for now. So if you find the bugs for the lint - report it to us.
5. Only one `@ConductorFactory` annotated factory and one `@ConductorBindingModule` annotated Dagger module are allowed per project for now.

### Retain View Modes
`setRetainViewMode` can be called on a `Controller` with one of two values: `RELEASE_DETACH`, which will release the `Controller`'s view as soon as it is detached from the screen (saves memory), or `RETAIN_DETACH`, which will ensure that a `Controller` holds on to its view, even if it's not currently shown on the screen (good for views that are expensive to re-create).

### Custom Change Handlers
`ControllerChangeHandler` can be subclassed in order to perform different functions when changing between two `Controllers`. Two convenience `ControllerChangeHandler` subclasses are included to cover most basic needs: `AnimatorChangeHandler`, which will use an `Animator` object to transition between two views, and `TransitionChangeHandler`, which will use Lollipop's `Transition` framework for transitioning between views.

### Child Routers & Controllers
`getChildRouter` can be called on a `Controller` in order to get a nested `Router` into which child `Controller`s can be pushed. This enables creating advanced layouts, such as Master/Detail.

### Glide Lifecycle
If the Glide dependency has been added, you need to setup Glide:
```java
public class GlideControllerSupport extends BaseGlideControllerSupport<GlideRequests> {

  public GlideControllerSupport(Controller controller) {
    super(controller);
  }

  @Override protected GlideRequests getGlideRequest(@NonNull ControllerLifecycle lifecycle, RequestManagerTreeNode requestManagerTreeNode) {
    Context context = App.getInstance().getApplicationContext();
    return new GlideRequests(Glide.get(context), lifecycle, requestManagerTreeNode, context);
  }
}
```

```java
public class HomeController extends Controller implements GlideProvider<GlideRequests>{

  private GlideControllerSupport glideControllerSupport = new GlideControllerSupport(this);

  @Override public GlideRequests getGlide() {
    return glideControllerSupport.getGlide();
  }

  @NonNull @Override protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {

    getGlide().load("url")
      .centerCrop()
      .into(new SimpleTarget<Drawable>() {
        @Override public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {

        }
      });

    return null;
  }
}
```

## License
```
Copyright (c) 2020 Lalafo.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
