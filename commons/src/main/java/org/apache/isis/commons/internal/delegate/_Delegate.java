package org.apache.isis.commons.internal.delegate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.util.ClassUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Creates a proxy that delegates method calls to a delegate instance.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
@UtilityClass
public class _Delegate {

    /**
     * For given {@code bluePrint} creates an instance, that implements all interfaces of
     * {@code bluePrint}, while delegating method calls to the {@code delegate} object.
     * The {@code delegate} object, does not necessarily need to implement any of the
     * {@code bluePrint}'s interfaces.
     * <p>
     * Particularly useful in connection with {@link lombok.experimental.Delegate}.
     * @param <T>
     * @param bluePrint
     * @param delegate
     * @see lombok.experimental.Delegate
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(final Class<T> bluePrint, final Object delegate) {
        Class<?>[] ifcs = ClassUtils.getAllInterfacesForClass(
                bluePrint, bluePrint.getClassLoader());
        return (T) Proxy.newProxyInstance(
                delegate.getClass().getClassLoader(), ifcs,
                new DelegatingInvocationHandler(delegate));
    }

    @RequiredArgsConstructor
    static class DelegatingInvocationHandler implements InvocationHandler {

        final Object delegate;

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable {

            if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if (method.getName().equals("hashCode")) {
                // Use hashCode of proxy rather than the delegate.
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("toString")) {
                // delegate Object.toString() method
                return "Proxy(" + delegate.toString() + ")";
            }

            // Invoke method with same signature on delegate
            try {
                val delegateMethod = delegate.getClass()
                        .getDeclaredMethod(method.getName(), method.getParameterTypes());
                return delegateMethod.invoke(delegate, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


}
