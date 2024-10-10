package com.ljt.study.filter;

/**
 * @author LiJingTang
 * @date 2024-08-21 09:43
 */
public final class ThreadLocalHolder {

    private ThreadLocalHolder() {}

    private static final ThreadLocal<String> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static String getAndRemove() {
        String context = get();
        remove();
        return context;
    }

    public static String get() {
        return THREAD_LOCAL.get();
    }

    public static void set(String context) {
        THREAD_LOCAL.set(context);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
