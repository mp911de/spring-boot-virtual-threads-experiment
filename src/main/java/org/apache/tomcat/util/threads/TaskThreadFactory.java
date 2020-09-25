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
package org.apache.tomcat.util.threads;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.loomservlet.GlobalThreadFactory;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.security.PrivilegedSetTccl;

public class TaskThreadFactory implements ThreadFactory {

    private static final Log log = LogFactory.getLog(TaskThread.class);

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private final int threadPriority;

    public TaskThreadFactory(String namePrefix, boolean daemon, int priority) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
        this.daemon = daemon;
        this.threadPriority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {

        Thread t = GlobalThreadFactory.create(new WrappingRunnable(r), namePrefix + threadNumber.getAndIncrement(), threadPriority, daemon);

        // Set the context class loader of newly created threads to be the class
        // loader that loaded this factory. This avoids retaining references to
        // web application class loaders and similar.
        if (Constants.IS_SECURITY_ENABLED) {
            PrivilegedAction<Void> pa = new PrivilegedSetTccl(
                    t, getClass().getClassLoader());
            AccessController.doPrivileged(pa);
        } else {
            t.setContextClassLoader(getClass().getClassLoader());
        }

        return t;
    }

    /**
     * Wraps a {@link Runnable} to swallow any {@link StopPooledThreadException}
     * instead of letting it go and potentially trigger a break in a debugger.
     */
    private static class WrappingRunnable implements Runnable {
        private Runnable wrappedRunnable;
        WrappingRunnable(Runnable wrappedRunnable) {
            this.wrappedRunnable = wrappedRunnable;
        }
        @Override
        public void run() {
            try {
                wrappedRunnable.run();
            } catch(StopPooledThreadException exc) {
                //expected : we just swallow the exception to avoid disturbing
                //debuggers like eclipse's
                log.debug("Thread exiting on purpose", exc);
            }
        }

    }
}
