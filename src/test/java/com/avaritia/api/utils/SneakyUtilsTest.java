package com.avaritia.api.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SneakyUtils 纯 Java 异常与函数式工具")
class SneakyUtilsTest {

    // region Static Null / Placeholder helpers

    @Test
    @DisplayName("none 返回不做任何事的 Runnable")
    void noneReturnsNoopRunnable() {
        Runnable r = SneakyUtils.none();
        assertNotNull(r);
        assertDoesNotThrow(r::run, "none() Runnable should not throw");
    }

    @Test
    @DisplayName("nullC 返回总是返回 null 的 Callable")
    void nullCReturnsNullCallable() throws Exception {
        Callable<String> c = SneakyUtils.nullC();
        assertNull(c.call(), "nullC Callable should return null");
    }

    @Test
    @DisplayName("nullS 返回总是返回 null 的 Supplier")
    void nullSReturnsNullSupplier() {
        Supplier<String> s = SneakyUtils.nullS();
        assertNull(s.get(), "nullS Supplier should return null");
    }

    @Test
    @DisplayName("nullCons 返回不做任何事的 Consumer")
    void nullConsReturnsNoopConsumer() {
        Consumer<String> c = SneakyUtils.nullCons();
        assertDoesNotThrow(() -> c.accept("doesnt matter"),
                "nullCons Consumer should not throw");
    }

    @Test
    @DisplayName("trueP 返回永远通过的 Predicate")
    void truePReturnsAlwaysTruePredicate() {
        Predicate<Object> p = SneakyUtils.trueP();
        assertTrue(p.test(null));
        assertTrue(p.test("anything"));
        assertTrue(p.test(42));
    }

    @Test
    @DisplayName("first 总是返回左侧元素")
    void firstReturnsLeftElement() {
        BinaryOperator<String> op = SneakyUtils.first();
        assertEquals("left", op.apply("left", "right"));
        assertEquals("primary", op.apply("primary", "fallback"));
    }

    @Test
    @DisplayName("last 总是返回右侧元素")
    void lastReturnsRightElement() {
        BinaryOperator<String> op = SneakyUtils.last();
        assertEquals("right", op.apply("left", "right"));
        assertEquals("fallback", op.apply("primary", "fallback"));
    }

    @Test
    @DisplayName("concat 按顺序执行两个 Runnable")
    void concatRunsBothRunnablesInOrder() {
        AtomicInteger seq = new AtomicInteger(0);
        Runnable a = () -> assertEquals(0, seq.getAndSet(1), "a should see initial 0");
        Runnable b = () -> assertEquals(1, seq.getAndSet(2), "b should see 1 after a");

        Runnable combined = SneakyUtils.concat(a, b);
        combined.run();
        assertEquals(2, seq.get(), "Both runnables should have executed");
    }

    @Test
    @DisplayName("notPossible 返回 NotPossibleException 的 Supplier")
    void notPossibleReturnsNotPossibleExceptionSupplier() {
        Supplier<SneakyUtils.NotPossibleException> sup = SneakyUtils.notPossible();
        SneakyUtils.NotPossibleException ex = sup.get();
        assertNotNull(ex);
        assertSame(SneakyUtils.NotPossibleException.INSTANCE, ex,
                "notPossible() should return the singleton INSTANCE");
    }

    // endregion

    // region Sneaky exception rethrow (execution)

    @Test
    @DisplayName("sneaky(ThrowingRunnable) 将受检异常重新抛为非受检")
    void sneakyThrowingRunnableRethrowsAsUnchecked() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                SneakyUtils.sneaky(() -> {
                    throw new Exception("checked failure");
                })
        );
        assertEquals("checked failure", thrown.getMessage());
    }

    @Test
    @DisplayName("sneaky(ThrowingRunnable) 正常执行时不抛异常")
    void sneakyThrowingRunnableDoesNotThrowOnSuccess() {
        AtomicInteger counter = new AtomicInteger();
        assertDoesNotThrow(() -> SneakyUtils.sneaky(counter::incrementAndGet));
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("sneaky(ThrowingSupplier) 返回正常值")
    void sneakyThrowingSupplierReturnsValue() {
        String result = SneakyUtils.sneaky(() -> "cosmic");
        assertEquals("cosmic", result);
    }

    @Test
    @DisplayName("sneaky(ThrowingSupplier) 将异常重新抛为非受检")
    void sneakyThrowingSupplierRethrowsAsUnchecked() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                SneakyUtils.sneaky((SneakyUtils.ThrowingSupplier<String, Throwable>) () -> {
                    throw new IllegalStateException("supplier failure");
                })
        );
        assertTrue(thrown instanceof IllegalStateException);
        assertEquals("supplier failure", thrown.getMessage());
    }

    // endregion

    // region Sneak Wrappers (produce new functional interface)

    @Test
    @DisplayName("sneak(ThrowingRunnable) 将受检异常 Runnable 包裹为标准 Runnable")
    void sneakThrowingRunnableWrapsToStandardRunnable() {
        AtomicInteger counter = new AtomicInteger();
        Runnable r = SneakyUtils.sneak(counter::incrementAndGet);
        r.run();
        assertEquals(1, counter.get());

        Runnable bomb = SneakyUtils.sneak(() -> {
            throw new Exception("wrapped failure");
        });
        RuntimeException ex = assertThrows(RuntimeException.class, bomb::run);
        assertEquals("wrapped failure", ex.getMessage());
    }

    @Test
    @DisplayName("sneak(ThrowingConsumer) 将受检异常 Consumer 包裹为标准 Consumer")
    void sneakThrowingConsumerWrapsToStandardConsumer() {
        AtomicReference<String> captured = new AtomicReference<>();
        Consumer<String> cons = SneakyUtils.sneak(captured::set);
        cons.accept("infinity");

        assertEquals("infinity", captured.get());

        Consumer<String> bomb = SneakyUtils.sneak((String s) -> {
            throw new Exception("consumer failure: " + s);
        });
        RuntimeException ex = assertThrows(RuntimeException.class, () -> bomb.accept("test"));
        assertEquals("consumer failure: test", ex.getMessage());
    }

    @Test
    @DisplayName("sneak(ThrowingSupplier) 将受检异常 Supplier 包裹为标准 Supplier")
    void sneakThrowingSupplierWrapsToStandardSupplier() {
        Supplier<Integer> sup = SneakyUtils.sneak(() -> 7);
        assertEquals(7, sup.get());

        Supplier<String> bomb = SneakyUtils.sneak((SneakyUtils.ThrowingSupplier<String, Throwable>) () -> {
            throw new IllegalAccessException("supplier issue");
        });
        RuntimeException ex = assertThrows(RuntimeException.class, bomb::get);
        assertTrue(ex instanceof IllegalAccessException);
    }

    @Test
    @DisplayName("sneak(ThrowingFunction) 将受检异常 Function 包裹为标准 Function")
    void sneakThrowingFunctionWrapsToStandardFunction() {
        Function<String, Integer> len = SneakyUtils.sneak(String::length);
        assertEquals(8, len.apply("avaritia"));

        Function<String, String> bomb = SneakyUtils.sneak((String s) -> {
            throw new Exception("function failure");
        });
        RuntimeException ex = assertThrows(RuntimeException.class, () -> bomb.apply("x"));
        assertEquals("function failure", ex.getMessage());
    }

    // endregion

    // region unsafeCast & throwUnchecked

    @Test
    @DisplayName("unsafeCast 对 null 输入返回 null")
    void unsafeCastReturnsNullForNullInput() {
        assertNull(SneakyUtils.unsafeCast(null));
    }

    @Test
    @DisplayName("unsafeCast 对非 null 输入返回原引用")
    void unsafeCastReturnsSameReferenceForNonNull() {
        String original = "original";
        Object casted = SneakyUtils.unsafeCast(original);
        assertSame(original, casted, "unsafeCast should return the same object reference");
    }

    @Test
    @DisplayName("unsafeCast 允许跨类型引用（编译期擦除）")
    void unsafeCastAllowsCrossTypeReference() {
        Integer num = 42;
        // This compiles but the returned reference is still the Integer
        String s = SneakyUtils.unsafeCast(num);
        assertSame(num, s, "unsafeCast should return the identical object");
        assertTrue(s instanceof Integer, "the object is still an Integer at runtime");
    }

    @Test
    @DisplayName("throwUnchecked 将任意 Throwable 抛出为未经检查的异常")
    void throwUncheckedThrowsAnyThrowableAsUnchecked() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                SneakyUtils.throwUnchecked(new Exception("original cause"))
        );
        assertTrue(thrown instanceof Exception);
        assertEquals("original cause", thrown.getMessage());
    }

    @Test
    @DisplayName("throwUnchecked 可抛出 Error 子类")
    void throwUncheckedThrowsErrorSubclass() {
        assertThrows(AssertionError.class, () ->
                SneakyUtils.throwUnchecked(new AssertionError("custom assertion"))
        );
    }

    // endregion

    // region NotPossibleException

    @Test
    @DisplayName("NotPossibleException 单例在所有调用间共享")
    void notPossibleExceptionIsSingleton() {
        SneakyUtils.NotPossibleException a = SneakyUtils.NotPossibleException.INSTANCE;
        SneakyUtils.NotPossibleException b = SneakyUtils.notPossible().get();
        assertSame(a, b, "Singleton INSTANCE should be reused");
    }

    @Test
    @DisplayName("NotPossibleException 是无参 RuntimeException 子类")
    void notPossibleExceptionIsRuntimeException() {
        assertTrue(SneakyUtils.NotPossibleException.INSTANCE instanceof RuntimeException);
        assertNull(SneakyUtils.NotPossibleException.INSTANCE.getMessage());
    }

    @Test
    @DisplayName("NotPossibleException 可携带自定义消息")
    void notPossibleExceptionConstructorAcceptsMessage() {
        SneakyUtils.NotPossibleException ex = new SneakyUtils.NotPossibleException("blame the cosmos");
        assertEquals("blame the cosmos", ex.getMessage());
    }

    // endregion

    // region Interface contracts

    @Test
    @DisplayName("ThrowingRunnable 可构造并正常完成")
    void throwingRunnableCompletesNormally() throws Exception {
        SneakyUtils.ThrowingRunnable<Exception> tr = () -> {
        };
        assertDoesNotThrow(tr::run);
    }

    @Test
    @DisplayName("ThrowingConsumer 可构造并正常消费")
    void throwingConsumerAcceptsValue() throws Exception {
        AtomicReference<String> capture = new AtomicReference<>();
        SneakyUtils.ThrowingConsumer<String, RuntimeException> tc = capture::set;
        tc.accept("pixels");
        assertEquals("pixels", capture.get());
    }

    @Test
    @DisplayName("ThrowingSupplier 提供值")
    void throwingSupplierProvidesValue() throws Exception {
        SneakyUtils.ThrowingSupplier<String, RuntimeException> ts = () -> "galactic";
        assertEquals("galactic", ts.get());
    }

    @Test
    @DisplayName("ThrowingFunction 对输入计算并返回结果")
    void throwingFunctionTransformsValue() throws Exception {
        SneakyUtils.ThrowingFunction<Integer, String, RuntimeException> tf = String::valueOf;
        assertEquals("256", tf.apply(256));
    }

    // endregion
}
