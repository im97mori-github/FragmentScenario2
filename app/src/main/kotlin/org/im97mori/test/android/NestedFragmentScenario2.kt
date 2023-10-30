package org.im97mori.test.android

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.commitNow
import androidx.fragment.app.testing.EmptyFragmentActivity
import androidx.fragment.app.testing.FragmentFactoryHolderViewModel
import androidx.fragment.testing.manifest.R
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.im97mori.test.android.FragmentScenario2.Companion.launch
import org.im97mori.test.android.NestedFragmentScenario2.Companion.launch
import java.io.Closeable

/**
 * Launches a Fragment with given arguments hosted by an empty [FragmentActivity] using
 * given [FragmentFactory] and waits for it to reach [initialState].
 *
 * This method cannot be called from the main thread.
 *
 * @param fragmentArgs a bundle to passed into fragment
 * @param themeResId a style resource id to be set to the host activity's theme
 * @param initialState the initial [Lifecycle.State]. Passing in
 * [DESTROYED][Lifecycle.State.DESTROYED] will result in an [IllegalArgumentException].
 * @param factory a fragment factory to use or null to use default factory
 */
public inline fun <reified F : Fragment, reified A : FragmentActivity, reified P : Fragment> launchNestedFragment(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
    factory: FragmentFactory? = null
): NestedFragmentScenario2<F, A, P> = NestedFragmentScenario2.launch(
    F::class.java, fragmentArgs, A::class.java, P::class.java, themeResId, initialState, factory
)

/**
 * Launches a Fragment with given arguments hosted by an empty [FragmentActivity] using
 * [instantiate] to create the Fragment and waits for it to reach [initialState].
 *
 * This method cannot be called from the main thread.
 *
 * @param fragmentArgs a bundle to passed into fragment
 * @param themeResId a style resource id to be set to the host activity's theme
 * @param initialState the initial [Lifecycle.State]. Passing in
 * [DESTROYED][Lifecycle.State.DESTROYED] will result in an [IllegalArgumentException].
 * @param instantiate method which will be used to instantiate the Fragment.
 */
public inline fun <reified F : Fragment, reified A : FragmentActivity, reified P : Fragment> launchNestedFragment(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
    crossinline instantiate: () -> F
): NestedFragmentScenario2<F, A, P> = NestedFragmentScenario2.launch(
    F::class.java, fragmentArgs, A::class.java, P::class.java, themeResId, initialState,
    object : FragmentFactory() {
        override fun instantiate(
            classLoader: ClassLoader,
            className: String
        ) = when (className) {
            F::class.java.name -> instantiate()
            else -> super.instantiate(classLoader, className)
        }
    }
)

/**
 * Launches a Fragment in the Activity's root view container `android.R.id.content`, with
 * given arguments hosted by an empty [FragmentActivity] and waits for it to reach [initialState].
 *
 * This method cannot be called from the main thread.
 *
 * @param fragmentArgs a bundle to passed into fragment
 * @param themeResId a style resource id to be set to the host activity's theme
 * @param initialState the initial [Lifecycle.State]. Passing in
 * [DESTROYED][Lifecycle.State.DESTROYED] will result in an [IllegalArgumentException].
 * @param factory a fragment factory to use or null to use default factory
 */
public inline fun <reified F : Fragment, reified A : FragmentActivity, reified P : Fragment> launchNestedFragmentInContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
    factory: FragmentFactory? = null
): NestedFragmentScenario2<F, A, P> = NestedFragmentScenario2.launchInContainer(
    F::class.java, fragmentArgs, A::class.java, P::class.java, themeResId, initialState, factory
)

/**
 * Launches a Fragment in the Activity's root view container `android.R.id.content`, with
 * given arguments hosted by an empty [FragmentActivity] using
 * [instantiate] to create the Fragment and waits for it to reach [initialState].
 *
 * This method cannot be called from the main thread.
 *
 * @param fragmentArgs a bundle to passed into fragment
 * @param themeResId a style resource id to be set to the host activity's theme
 * @param initialState the initial [Lifecycle.State]. Passing in
 * [DESTROYED][Lifecycle.State.DESTROYED] will result in an [IllegalArgumentException].
 * @param instantiate method which will be used to instantiate the Fragment. This is a
 * simplification of the [FragmentFactory] interface for cases where only a single class
 * needs a custom constructor called.
 */
public inline fun <reified F : Fragment, reified A : FragmentActivity, reified P : Fragment> launchNestedFragmentInContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED,
    crossinline instantiate: () -> F
): NestedFragmentScenario2<F, A, P> = NestedFragmentScenario2.launchInContainer(
    F::class.java, fragmentArgs, A::class.java, P::class.java, themeResId, initialState,
    object : FragmentFactory() {
        override fun instantiate(
            classLoader: ClassLoader,
            className: String
        ) = when (className) {
            F::class.java.name -> instantiate()
            else -> super.instantiate(classLoader, className)
        }
    }
)

/**
 * Run [block] using [FragmentScenario2.onFragment], returning the result of the [block].
 *
 * If any exceptions are raised while running [block], they are rethrown.
 */
@SuppressWarnings("DocumentExceptions")
public inline fun <reified F : Fragment, A : FragmentActivity, P : Fragment, T : Any> NestedFragmentScenario2<F, A, P>.withFragment(
    crossinline block: F.() -> T
): T {
    lateinit var value: T
    var err: Throwable? = null
    onFragment { fragment ->
        try {
            value = block(fragment)
        } catch (t: Throwable) {
            err = t
        }
    }
    err?.let { throw it }
    return value
}

/**
 * FragmentScenario2 provides API to start and drive a Fragment's lifecycle state for testing. It
 * works with arbitrary fragments and works consistently across different versions of the Android
 * framework.
 *
 * FragmentScenario2 only supports [androidx.fragment.app.Fragment][Fragment]. If you are using
 * a deprecated fragment class such as `android.support.v4.app.Fragment` or
 * [android.app.Fragment], please update your code to
 * [androidx.fragment.app.Fragment][Fragment].
 *
 * If your testing Fragment has a dependency to specific theme such as `Theme.AppCompat`,
 * use the theme ID parameter in [launch] method.
 *
 * @param F The Fragment class being tested
 *
 * @see ActivityScenario a scenario API for Activity
 */
@SuppressLint("PrivateResource")
public class NestedFragmentScenario2<F : Fragment, A : FragmentActivity, P : Fragment> private constructor(
    @Suppress("MemberVisibilityCanBePrivate") /* synthetic access */
    internal val fragmentClass: Class<F>,
    private val activityScenario: ActivityScenario<A>
) : Closeable {

    /**
     * Moves Fragment state to a new state.
     *
     *  If a new state and current state are the same, this method does nothing. It accepts
     * all [Lifecycle.State]s. [DESTROYED][Lifecycle.State.DESTROYED] is a terminal state.
     * You cannot move to any other state after the Fragment reaches that state.
     *
     * This method cannot be called from the main thread.
     */
    public fun moveToState(newState: Lifecycle.State): NestedFragmentScenario2<F, A, P> {
        if (newState == Lifecycle.State.DESTROYED) {
            activityScenario.onActivity { activity ->
                val parentFragment =
                    activity.supportFragmentManager.findFragmentByTag(PARENT_FRAGMENT_TAG);
                if (parentFragment != null) {
                    val fragment =
                        parentFragment.childFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                    if (fragment != null) {
                        parentFragment.childFragmentManager.commitNow {
                            remove(fragment)
                        }
                    }
                }
            }
        } else {
            activityScenario.onActivity { activity ->
                val parentFragment =
                    activity.supportFragmentManager.findFragmentByTag(PARENT_FRAGMENT_TAG);
                if (parentFragment != null) {
                    val fragment =
                        requireNotNull(
                            parentFragment.childFragmentManager.findFragmentByTag(
                                FRAGMENT_TAG
                            )
                        ) {
                            "The fragment has been removed from the FragmentManager already."
                        }
                    parentFragment.childFragmentManager.commitNow {
                        setMaxLifecycle(fragment, newState)
                    }
                }
            }
        }
        return this
    }

    /**
     * Recreates the host Activity.
     *
     * After this method call, it is ensured that the Fragment state goes back to the same state
     * as its previous state.
     *
     * This method cannot be called from the main thread.
     */
    public fun recreate(): NestedFragmentScenario2<F, A, P> {
        activityScenario.recreate()
        return this
    }

    /**
     * FragmentAction interface should be implemented by any class whose instances are intended to
     * be executed by the main thread. A Fragment that is instrumented by the FragmentScenario2 is
     * passed to [FragmentAction.perform] method.
     *
     * You should never keep the Fragment reference as it will lead to unpredictable behaviour.
     * It should only be accessed in [FragmentAction.perform] scope.
     */
    public fun interface FragmentAction<F : Fragment> {
        /**
         * This method is invoked on the main thread with the reference to the Fragment.
         *
         * @param fragment a Fragment instrumented by the FragmentScenario2.
         */
        public fun perform(fragment: F)
    }

    /**
     * Runs a given [action] on the current Activity's main thread.
     *
     * Note that you should never keep Fragment reference passed into your [action]
     * because it can be recreated at anytime during state transitions.
     *
     * Throwing an exception from [action] makes the host Activity crash. You can
     * inspect the exception in logcat outputs.
     *
     * This method cannot be called from the main thread.
     */
    public fun onFragment(action: FragmentAction<F>): NestedFragmentScenario2<F, A, P> {
        activityScenario.onActivity { activity ->
            val parentFragment =
                activity.supportFragmentManager.findFragmentByTag(PARENT_FRAGMENT_TAG);
            if (parentFragment != null) {
                val fragment = requireNotNull(
                    parentFragment.childFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                ) {
                    "The fragment has been removed from the FragmentManager already."
                }
                check(fragmentClass.isInstance(fragment))
                action.perform(requireNotNull(fragmentClass.cast(fragment)))
            }
        }
        return this
    }

    /**
     * Finishes the managed fragments and cleans up device's state. This method blocks execution
     * until the host activity becomes [Lifecycle.State.DESTROYED].
     */
    public override fun close() {
        activityScenario.close()
    }

    public companion object {
        private const val PARENT_FRAGMENT_TAG = "NestedFragmentScenario2_ParentFragment_Tag"
        private const val FRAGMENT_TAG = "NestedFragmentScenario2_Fragment_Tag"

        /**
         * Launches a Fragment with given arguments hosted by an empty [FragmentActivity] using
         * the given [FragmentFactory] and waits for it to reach the resumed state.
         *
         *
         * This method cannot be called from the main thread.
         *
         * @param fragmentClass a fragment class to instantiate
         * @param fragmentArgs a bundle to passed into fragment
         * @param factory a fragment factory to use or null to use default factory
         */
        @JvmStatic
        public fun <F : Fragment, A : FragmentActivity, P : Fragment> launch(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle?,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            factory: FragmentFactory?
        ): NestedFragmentScenario2<F, A, P> = launch(
            fragmentClass,
            fragmentArgs,
            activityClass,
            parentFragmentClass,
            R.style.FragmentScenarioEmptyFragmentActivityTheme,
            Lifecycle.State.RESUMED,
            factory
        )

        /**
         * Launches a Fragment with given arguments hosted by an empty [FragmentActivity] themed
         * by [themeResId], using the given [FragmentFactory] and waits for it to reach the
         * resumed state.
         *
         * This method cannot be called from the main thread.
         *
         * @param fragmentClass a fragment class to instantiate
         * @param fragmentArgs a bundle to passed into fragment
         * @param themeResId a style resource id to be set to the host activity's theme
         * @param factory a fragment factory to use or null to use default factory
         */
        @JvmStatic
        public fun <F : Fragment, A : FragmentActivity, P : Fragment> launch(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle?,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            @StyleRes themeResId: Int,
            factory: FragmentFactory?
        ): NestedFragmentScenario2<F, A, P> = launch(
            fragmentClass,
            fragmentArgs,
            activityClass,
            parentFragmentClass,
            themeResId,
            Lifecycle.State.RESUMED,
            factory
        )

        /**
         * Launches a Fragment with given arguments hosted by an empty [FragmentActivity] themed
         * by [themeResId], using the given [FragmentFactory] and waits for it to reach
         * [initialState].
         *
         * This method cannot be called from the main thread.
         *
         * @param fragmentClass a fragment class to instantiate
         * @param fragmentArgs a bundle to passed into fragment
         * @param themeResId a style resource id to be set to the host activity's theme
         * @param initialState The initial [Lifecycle.State]. Passing in
         * [DESTROYED][Lifecycle.State.DESTROYED] will result in an [IllegalArgumentException].
         * @param factory a fragment factory to use or null to use default factory
         */
        @JvmOverloads
        @JvmStatic
        public fun <F : Fragment, A : FragmentActivity, P : Fragment> launch(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle? = null,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
            initialState: Lifecycle.State = Lifecycle.State.RESUMED,
            factory: FragmentFactory? = null
        ): NestedFragmentScenario2<F, A, P> = internalLaunch(
            fragmentClass,
            fragmentArgs,
            activityClass,
            parentFragmentClass,
            themeResId,
            initialState,
            factory,
            0 /*containerViewId=*/,
            0
        )

        /**
         * Launches a Fragment in the Activity's root view container `android.R.id.content`, with
         * given arguments hosted by an empty [FragmentActivity] using the given
         * [FragmentFactory] and waits for it to reach the resumed state.
         *
         * This method cannot be called from the main thread.
         *
         * @param fragmentClass a fragment class to instantiate
         * @param fragmentArgs a bundle to passed into fragment
         * @param factory a fragment factory to use or null to use default factory
         */
        @JvmStatic
        public fun <F : Fragment, A : FragmentActivity, P : Fragment> launchInContainer(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle?,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            factory: FragmentFactory?,
            childContainerViewId: Int
        ): NestedFragmentScenario2<F, A, P> = launchInContainer(
            fragmentClass,
            fragmentArgs,
            activityClass,
            parentFragmentClass,
            R.style.FragmentScenarioEmptyFragmentActivityTheme,
            Lifecycle.State.RESUMED,
            factory,
            childContainerViewId
        )

        /**
         * Launches a Fragment in the Activity's root view container `android.R.id.content`, with
         * given arguments hosted by an empty [FragmentActivity] themed by [themeResId],
         * using the given [FragmentFactory] and waits for it to reach the resumed state.
         *
         * This method cannot be called from the main thread.
         *
         * @param fragmentClass a fragment class to instantiate
         * @param fragmentArgs a bundle to passed into fragment
         * @param themeResId a style resource id to be set to the host activity's theme
         * @param factory a fragment factory to use or null to use default factory
         */
        @JvmStatic
        public fun <F : Fragment, A : FragmentActivity, P : Fragment> launchInContainer(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle?,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            @StyleRes themeResId: Int,
            factory: FragmentFactory?,
            childContainerViewId: Int
        ): NestedFragmentScenario2<F, A, P> = launchInContainer(
            fragmentClass,
            fragmentArgs,
            activityClass,
            parentFragmentClass,
            themeResId,
            Lifecycle.State.RESUMED,
            factory,
            childContainerViewId
        )

        /**
         * Launches a Fragment in the Activity's root view container `android.R.id.content`, with
         * given arguments hosted by an empty [FragmentActivity] themed by [themeResId],
         * using the given [FragmentFactory] and waits for it to reach [initialState].
         *
         * This method cannot be called from the main thread.
         *
         * @param fragmentClass a fragment class to instantiate
         * @param fragmentArgs a bundle to passed into fragment
         * @param themeResId a style resource id to be set to the host activity's theme
         * @param initialState The initial [Lifecycle.State]. Passing in
         * [DESTROYED][Lifecycle.State.DESTROYED] will result in an [IllegalArgumentException].
         * @param factory a fragment factory to use or null to use default factory
         */
        @JvmOverloads
        @JvmStatic
        public fun <F : Fragment, A : FragmentActivity, P : Fragment> launchInContainer(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle? = null,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
            initialState: Lifecycle.State = Lifecycle.State.RESUMED,
            factory: FragmentFactory? = null,
            childContainerViewId: Int = 0
        ): NestedFragmentScenario2<F, A, P> = internalLaunch(
            fragmentClass,
            fragmentArgs,
            activityClass,
            parentFragmentClass,
            themeResId,
            initialState,
            factory,
            android.R.id.content,
            childContainerViewId
        )

        @SuppressLint("RestrictedApi")
        internal fun <F : Fragment, A : FragmentActivity, P : Fragment> internalLaunch(
            fragmentClass: Class<F>,
            fragmentArgs: Bundle?,
            activityClass: Class<A>,
            parentFragmentClass: Class<P>,
            @StyleRes themeResId: Int,
            initialState: Lifecycle.State,
            factory: FragmentFactory?,
            @IdRes containerViewId: Int,
            @IdRes childContainerViewId: Int
        ): NestedFragmentScenario2<F, A, P> {
            require(initialState != Lifecycle.State.DESTROYED) {
                "Cannot set initial Lifecycle state to $initialState for FragmentScenario2"
            }
            val componentName = ComponentName(
                ApplicationProvider.getApplicationContext(),
                activityClass
            )
            val startActivityIntent = Intent.makeMainActivity(componentName)
                .putExtra(EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, themeResId)
            val activityScenario = ActivityScenario.launch<A>(startActivityIntent)
            val scenario = NestedFragmentScenario2<F, A, P>(
                fragmentClass,
                activityScenario
            )
            scenario.activityScenario.onActivity { activity ->
                if (factory != null) {
                    FragmentFactoryHolderViewModel.getInstance(activity).fragmentFactory = factory
                    activity.supportFragmentManager.fragmentFactory = factory
                }
                val parentFragment = activity.supportFragmentManager.fragmentFactory
                    .instantiate(
                        requireNotNull(parentFragmentClass.classLoader),
                        parentFragmentClass.name
                    )

                activity.supportFragmentManager.commitNow {
                    add(containerViewId, parentFragment, PARENT_FRAGMENT_TAG)
                    setMaxLifecycle(parentFragment, Lifecycle.State.STARTED)
                }
                if (factory != null) {
                    parentFragment.childFragmentManager.fragmentFactory = factory
                }
                val fragment = parentFragment.childFragmentManager.fragmentFactory
                    .instantiate(requireNotNull(fragmentClass.classLoader), fragmentClass.name)
                fragment.arguments = fragmentArgs
                parentFragment.childFragmentManager.commitNow {
                    add(childContainerViewId, fragment, FRAGMENT_TAG)
                    setMaxLifecycle(fragment, initialState)
                }
            }
            return scenario
        }
    }
}
