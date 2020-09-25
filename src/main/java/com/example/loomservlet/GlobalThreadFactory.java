/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.loomservlet;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom {@link ThreadFactory} that is used from all the places that create threads.
 * Setting the {@code -DvirtualThreads=true} turns each thread into a virtual one.
 *
 * @author Mark Paluch
 */
public class GlobalThreadFactory implements ThreadFactory {

	// We want to limit the number of kernel threads that host our VirtualThreads.
	// If we don't set the executor, a default ForkJoin pool gets spun up that seems to be subject to unbounded growth.
	public static Executor host = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static boolean useVirtualThreads = Boolean
			.parseBoolean(System.getProperty("virtualThreads", "false"));

	// Just to record some stats.
	public static AtomicLong threadsCreated = new AtomicLong();

	public final AtomicLong threadCounter = new AtomicLong();
	private final String threadName;
	private final boolean daemon;

	public GlobalThreadFactory(String threadName, boolean daemon) {
		this.threadName = threadName;
		this.daemon = daemon;
	}

	static void customize(Thread.Builder builder) {

		// Note that VirtualThreads can't have a ThreadGroup
		if (useVirtualThreads) {
			builder.virtual(host);
		}
	}

	public static Thread create(Runnable runnable, String name) {

		Thread.Builder builder = Thread.builder().name(name).task(runnable);
		customize(builder);
		return doBuild(builder);
	}

	public static Thread create(Runnable runnable, String name, int threadPriority, boolean daemon) {

		Thread.Builder builder = Thread.builder().name(name).task(runnable)
				.priority(threadPriority).daemon(daemon);
		customize(builder);
		return doBuild(builder);
	}

	@Override
	public Thread newThread(Runnable runnable) {

		Thread.Builder builder = Thread.builder()
				.name(threadName + "-" + threadCounter.incrementAndGet())
				.task(runnable)
				.daemon(daemon);

		customize(builder);

		return doBuild(builder);
	}

	private static Thread doBuild(Thread.Builder builder) {

		Thread thread = builder.build();

		if (useVirtualThreads) {
			System.out.println("VirtualThread: " + threadsCreated
					.incrementAndGet() + " -> " + thread.getName());
		}
		else {
			System.out.println("Thread: " + threadsCreated
					.incrementAndGet() + " -> " + thread.getName());
		}
		return thread;
	}

}
