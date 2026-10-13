package com.codingchili.core.protocol;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Binds the annotated methods of a handler to something that a {@link Protocol} can invoke. The strategy is swappable
 * with {@link Protocol#bindings(BindingProvider)}, for example to compare the cost of the strategies in a benchmark.
 * <p>
 * Methods are made accessible by the protocol before they are passed to the provider.
 */
@FunctionalInterface
public interface BindingProvider {
    /**
     * Binds methods using {@link Method#invoke(Object, Object...)}, the default.
     */
    BindingProvider REFLECTION = method -> new Binding() {
        @Override
        public Object invoke(Object instance) throws Exception {
            try {
                return method.invoke(instance);
            } catch (InvocationTargetException e) {
                throw unwrap(e);
            }
        }

        @Override
        public Object invoke(Object instance, Object a) throws Exception {
            try {
                return method.invoke(instance, a);
            } catch (InvocationTargetException e) {
                throw unwrap(e);
            }
        }

        @Override
        public Object invoke(Object instance, Object a, Object b) throws Exception {
            try {
                return method.invoke(instance, a, b);
            } catch (InvocationTargetException e) {
                throw unwrap(e);
            }
        }
    };

    /**
     * Binds methods using {@link MethodHandle}s, which the JIT can inline when the handle is a constant.
     */
    BindingProvider METHOD_HANDLES = method -> {
        MethodHandle handle;
        try {
            handle = MethodHandles.lookup().unreflect(method)
                    .asType(MethodType.genericMethodType(method.getParameterCount() + 1));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(method + " is not accessible.", e);
        }
        return new Binding() {
            @Override
            public Object invoke(Object instance) throws Exception {
                try {
                    return (Object) handle.invokeExact(instance);
                } catch (Exception | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object invoke(Object instance, Object a) throws Exception {
                try {
                    return (Object) handle.invokeExact(instance, a);
                } catch (Exception | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object invoke(Object instance, Object a, Object b) throws Exception {
                try {
                    return (Object) handle.invokeExact(instance, a, b);
                } catch (Exception | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };
    };

    /**
     * Binds methods using {@link java.lang.invoke.LambdaMetafactory}: a class is generated for each method, which makes
     * binding the most expensive and invoking the cheapest. Methods that the generated classes don't support, see
     * {@link LambdaBindings}, are bound as {@link #METHOD_HANDLES}.
     */
    BindingProvider LAMBDAS = LambdaBindings::bind;

    /**
     * @param method the method to bind, accessible, with at most two parameters.
     * @return a binding that invokes the method.
     */
    Binding bind(Method method);

    private static Exception unwrap(InvocationTargetException e) {
        return (e.getCause() instanceof Exception cause) ? cause : e;
    }
}
