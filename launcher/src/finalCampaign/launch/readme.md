# Help us
If you entered this directory and looked at these code, it's proved that you're curious about how our mod works to modify the game.  
If you are familiar with Android developing and `Android DVM (Dalvik VM)`, please help us. We had two solutions to adapt our mod for Android, but all of them failed. The core reason of this result is `"Android jvm"(dvm) != jvm`. There are lots of restrictions on Android (They blacklisted many java apis and Android internal apis and fields), so our mod can not work like on desktop.  
To be honest, maybe our final way, I think, is to run a OpenJDK runtime on Android to launch the desktop version of Mindustry like what `PojavLauncher` does.
## How our mod works
Normal game process looks like this

```
Game Main -> Game Loop -> End
```

After we patched the game, it looks like this

```
Pre-Main -> Setup Mixins and ClassLoader ->
Game Main -> Game Loop -> End
```

We load the whole game in a customized classloader so we can patch the bytecode of the game as we need.  
  
For more details, see [Mixin](https://github.com/SpongePowered/Mixin). We're just a user of it.

## Our failed solutions
### solution 1
Our main activity extends AndroidApplication, so we init arc successfully without the activity problem (Arc AndroidApplication extends Activity and Android check the activity and find it from `App Classloader` instead of using the one we give it).  
But the problem is that official version of Mindustry enabled R8 during build, which merged parts of mindustry code into arc, rhino and other dependencies. That means arc will try to find Mindustry classes from `App Classloader`. But Mindustry is loaded by a customized classloader, so it won't be found and will lead to a crash.
### solution 2
We load the whole game in the customized classloader and the game keeps all the arc methods from being shinked by R8, so it won't crash like `solution 1`.  
But we can not start a `"dynamic"` activity, whose class is not in the application's dex. (Reflections for dynamic activity won't work on Android 11)  
So we try to construct a `AndroidLauncher (in Mindustry)` then establish a proxy between `our activity` and `AndroidApplication (in arc)`. We all think this should work, but we meet the biggest problem...

## Biggest problem
Since Android blacklisted something like `metaFactory(...)` in `LambdaMetafactory`, java lambda won't work without desugaring. And after that, some lambdas will be broke into a stand alone class named `-$$Lambda$[ClassName]$xxx` or simply named `-$$Lambda$xxx`, which can't be identified by Mixin so it won't be transformed. So the reference in it won't be patched. It leads to a `Verify Error` at runtime.   
This problem is so serious that even if we bring Mindustry up successfully and it's well-functioning, the lambda we introduced by mixin will mess all up.  
As Mindustry used lots of lambda, without lambda is our patch classes for mixin is not acceptable.  
So how to deal with the lambda? We don't have any good ideas now. 