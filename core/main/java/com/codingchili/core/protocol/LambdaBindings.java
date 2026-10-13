package com.codingchili.core.protocol;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Binds methods using {@link LambdaMetafactory}, see {@link BindingProvider#LAMBDAS}: a class is generated for each
 * method that calls it directly, which is the cheapest to invoke and the most expensive to bind.
 * <p>
 * Supports instance methods with up to two parameters that are not primitives, other methods are bound using
 * {@link BindingProvider#METHOD_HANDLES}. The generated classes implement the single method interfaces below,
 * which are public as the classes are generated in the package of the handler.
 */
public final class LambdaBindings {
    private static final int MAX_PARAMETERS = 2;

    private LambdaBindings() {
    }

    /**
     * A method with a result and no parameters.
     */
    @FunctionalInterface
    public interface Call0 {
        Object call(Object instance) throws Exception;
    }

    /**
     * A method with a result and one parameter.
     */
    @FunctionalInterface
    public interface Call1 {
        Object call(Object instance, Object a) throws Exception;
    }

    /**
     * A method with a result and two parameters.
     */
    @FunctionalInterface
    public interface Call2 {
        Object call(Object instance, Object a, Object b) throws Exception;
    }

    /**
     * A void method with no parameters.
     */
    @FunctionalInterface
    public interface Run0 {
        void run(Object instance) throws Exception;
    }

    /**
     * A void method with one parameter.
     */
    @FunctionalInterface
    public interface Run1 {
        void run(Object instance, Object a) throws Exception;
    }

    /**
     * A void method with two parameters.
     */
    @FunctionalInterface
    public interface Run2 {
        void run(Object instance, Object a, Object b) throws Exception;
    }

    /**
     * @param method the method to bind, accessible.
     * @return a binding that calls the method through a generated class.
     */
    static Binding bind(Method method) {
        if (!supported(method)) {
            return BindingProvider.METHOD_HANDLES.bind(method);
        }
        try {
            int count = method.getParameterCount();
            boolean isVoid = method.getReturnType() == void.class;
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle implementation = lookup.unreflect(method);

            Class<?> type = (isVoid) ? runType(count) : callType(count);
            MethodType erased = MethodType.genericMethodType(count + 1);
            if (isVoid) {
                erased = erased.changeReturnType(void.class);
            }

            Object function = LambdaMetafactory.metafactory(lookup, (isVoid) ? "run" : "call",
                            MethodType.methodType(type), erased, implementation, implementation.type())
                    .getTarget().invoke();

            return binding(function, count, isVoid);
        } catch (Throwable e) {
            throw new IllegalArgumentException("failed to bind " + method, e);
        }
    }

    private static boolean supported(Method method) {
        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() > MAX_PARAMETERS) {
            return false;
        }
        for (Class<?> parameter : method.getParameterTypes()) {
            if (parameter.isPrimitive()) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> callType(int count) {
        return switch (count) {
            case 0 -> Call0.class;
            case 1 -> Call1.class;
            default -> Call2.class;
        };
    }

    private static Class<?> runType(int count) {
        return switch (count) {
            case 0 -> Run0.class;
            case 1 -> Run1.class;
            default -> Run2.class;
        };
    }

    private static Binding binding(Object function, int count, boolean isVoid) {
        if (isVoid) {
            return switch (count) {
                case 0 -> new Binding() {
                    @Override
                    public Object invoke(Object instance) throws Exception {
                        ((Run0) function).run(instance);
                        return null;
                    }
                };
                case 1 -> new Binding() {
                    @Override
                    public Object invoke(Object instance, Object a) throws Exception {
                        ((Run1) function).run(instance, a);
                        return null;
                    }
                };
                default -> new Binding() {
                    @Override
                    public Object invoke(Object instance, Object a, Object b) throws Exception {
                        ((Run2) function).run(instance, a, b);
                        return null;
                    }
                };
            };
        }
        return switch (count) {
            case 0 -> new Binding() {
                @Override
                public Object invoke(Object instance) throws Exception {
                    return ((Call0) function).call(instance);
                }
            };
            case 1 -> new Binding() {
                @Override
                public Object invoke(Object instance, Object a) throws Exception {
                    return ((Call1) function).call(instance, a);
                }
            };
            default -> new Binding() {
                @Override
                public Object invoke(Object instance, Object a, Object b) throws Exception {
                    return ((Call2) function).call(instance, a, b);
                }
            };
        };
    }
}
