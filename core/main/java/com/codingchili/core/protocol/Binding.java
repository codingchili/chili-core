package com.codingchili.core.protocol;

/**
 * A bound handler method, created by a {@link BindingProvider}. Invoking it calls the method that it was bound to.
 * <p>
 * There is a method for each number of parameters, so that no array is allocated to pass the arguments. A binding
 * implements the method that matches the parameters of the method it is bound to, the others throw
 * {@link IllegalArgumentException}. Methods with more than two parameters are not supported.
 * <p>
 * The methods throw the exception thrown by the bound method itself, never a wrapper such as
 * {@link java.lang.reflect.InvocationTargetException}. They return the value returned by the method, null for void.
 */
public interface Binding {

    /**
     * @param instance the handler that the method belongs to.
     * @return the result of a method without parameters.
     * @throws Exception thrown by the method.
     */
    default Object invoke(Object instance) throws Exception {
        throw mismatch(0);
    }

    /**
     * @param instance the handler that the method belongs to.
     * @param a        the parameter.
     * @return the result of a method with one parameter.
     * @throws Exception thrown by the method.
     */
    default Object invoke(Object instance, Object a) throws Exception {
        throw mismatch(1);
    }

    /**
     * @param instance the handler that the method belongs to.
     * @param a        the first parameter.
     * @param b        the second parameter.
     * @return the result of a method with two parameters.
     * @throws Exception thrown by the method.
     */
    default Object invoke(Object instance, Object a, Object b) throws Exception {
        throw mismatch(2);
    }

    private static IllegalArgumentException mismatch(int count) {
        return new IllegalArgumentException("the bound method does not take " + count + " parameters.");
    }
}
