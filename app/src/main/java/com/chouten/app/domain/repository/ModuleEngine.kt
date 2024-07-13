package com.chouten.app.domain.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface ModuleEngine {
    /**
     * This allows the module to run asynchronous code without
     * relying on suspending functions.
     * For example, a result "foo" could be sent to flow "bar"
     * and collected from another location, rather than relying on a suspending
     * function within the ModuleEngine to return the value.
     */
    var observables: Map<String, MutableStateFlow<String>>

    /**
     * Scope used for launching updates to the observables.
     * For example, a `viewModelScope`.
     */
    var scope: CoroutineScope

    /**
     * Interceptors are additional callbacks which can be registered.
     * Available interceptors will differ between implementations of
     * the [ModuleEngine]
     */
    val interceptors: MutableMap<String, (Any) -> Unit>

    /**
     * Evaluates JavaScript in the engine and executes the callback with the evaluated value.
     * @param javascript The JavaScript code to evaluate
     * @param callback The callback to execute once the javascript has finished evaluation
     *
     * @return An optional Boolean, for whether or not the evaluation succeeded or not. A null value
     * is not to be interpreted as success nor failure. Implementations may not decide to implement
     * some form of error catching/success testing and thus return null.
     */
    fun evaluateJavascript(javascript: String, callback: (String) -> Unit): Boolean?

    /**
     * Loads JavaScript into the engine but does NOT evaluate it. May be used for injecting
     * some JS which is later used within an evaluation.
     * @param javascript The JavaScript to load into the engine
     *
     * @return An optional Boolean, for whether or not the evaluation succeeded or not. A null value
     *      * is not to be interpreted as success nor failure. Implementations may not decide to implement
     *      * some form of error catching/success testing and thus return null.
     */
    fun load(javascript: String): Boolean?

    /**
     * Registers an observable flow.
     * @param key Key of the flow. Used to access the flow to emit values to it.
     * @param value The reference to the mutable flow itself
     *
     * @return A Boolean value of success or failure, or null if the implementation
     * is unable to provide a value.
     */
    fun registerObservable(key: String, value: MutableStateFlow<String>): Boolean? {
        observables.containsKey(key).let {
            if (it) return false
            observables = observables + mapOf(key to value)
            return true
        }
    }

    /**
     * Registers an interceptor.
     * @param identifier Identifier of the interceptor. Used within the implementation to access the callback.
     * @param interceptor The callback run by the implementation
     *
     * @return A Boolean value of success or failure, or null if the implementation
     * is unable to provide a value.
     */
    fun registerInterceptor(identifier: String, interceptor: (Any) -> Unit): Boolean? {
        interceptors.containsKey(identifier).let {
            if (it) return false
            interceptors[identifier] = interceptor
            return true
        }
    }
}