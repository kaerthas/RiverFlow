package com.riverflow.admin.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 带信号量限流的虚拟线程执行器包装器。
 *
 * <p>底层使用 {@link Executors#newVirtualThreadPerTaskExecutor()}，每个任务独占一个虚拟线程；
 * 通过 {@link Semaphore} 限制同时运行的任务数，防止虚拟线程无界增长压垮外部资源
 * （如数据库连接池、HTTP 连接池、下游服务等）。
 *
 * <p>适用场景：I/O 密集型、任务大部分时间阻塞在外部调用、需要高并发但外部资源有限的任务队列。
 *
 * <p>注意事项：
 * <ul>
 *   <li>任务执行前会 acquire 许可，执行完毕后 finally 释放，异常流程也能保证释放。</li>
 *   <li>若调用方在任务内再次向本执行器提交子任务，需确保不会形成死锁（建议子任务使用独立执行器）。</li>
 *   <li> permits 数量应根据最稀缺的外部连接池容量设定，通常不超过数据库连接池 max-active 的 1~2 倍。</li>
 * </ul>
 */
public class SemaphoreExecutorService implements ExecutorService {

    private final ExecutorService delegate;
    private final Semaphore semaphore;
    private final int maxPermits;
    private final Counter submittedCounter;
    private final Counter rejectedCounter;

    public SemaphoreExecutorService(int permits) {
        this(permits, null);
    }

    public SemaphoreExecutorService(int permits, MeterRegistry meterRegistry) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }
        this.maxPermits = permits;
        this.delegate = Executors.newVirtualThreadPerTaskExecutor();
        this.semaphore = new Semaphore(permits);

        if (meterRegistry != null) {
            Gauge.builder("flow_executor_available_permits", semaphore, Semaphore::availablePermits)
                    .description("流程执行器可用信号量")
                    .register(meterRegistry);
            Gauge.builder("flow_executor_active_tasks", this, s -> (double) (maxPermits - semaphore.availablePermits()))
                    .description("流程执行器当前执行任务数")
                    .register(meterRegistry);
            this.submittedCounter = Counter.builder("flow_executor_submitted_total")
                    .description("流程执行器提交任务总数")
                    .register(meterRegistry);
            this.rejectedCounter = Counter.builder("flow_executor_rejected_total")
                    .description("流程执行器拒绝任务总数")
                    .register(meterRegistry);
        } else {
            this.submittedCounter = null;
            this.rejectedCounter = null;
        }
    }

    @Override
    public void execute(Runnable command) {
        if (submittedCounter != null) {
            submittedCounter.increment();
        }
        Runnable wrapped = wrapRunnable(command);
        delegate.execute(wrapped);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrapRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(wrapRunnable(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrapCallable(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(wrapCallables(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return delegate.invokeAll(wrapCallables(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return delegate.invokeAny(wrapCallables(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(wrapCallables(tasks), timeout, unit);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    private Runnable wrapRunnable(Runnable command) {
        return () -> {
            try {
                semaphore.acquire();
                command.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        };
    }

    private <T> Callable<T> wrapCallable(Callable<T> task) {
        return () -> {
            try {
                semaphore.acquire();
                return task.call();
            } finally {
                semaphore.release();
            }
        };
    }

    private <T> Collection<Callable<T>> wrapCallables(Collection<? extends Callable<T>> tasks) {
        if (tasks == null) {
            throw new NullPointerException("tasks collection is null");
        }
        List<Callable<T>> wrapped = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            wrapped.add(wrapCallable(task));
        }
        return wrapped;
    }
}
