package com.tonic.jaloc.memory.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A reachability backstop that frees allocations whose state was never closed, warning on stderr; -Djaloc.debug=true captures allocation sites.
 */
public final class NativeCleaner
{
    private static final boolean DEBUG = Boolean.getBoolean("jaloc.debug");

    private static final ReferenceQueue<AllocationState> QUEUE = new ReferenceQueue<>();

    private static final Set<Registration> REGISTRATIONS = ConcurrentHashMap.newKeySet();

    static {
        Thread thread = new Thread(NativeCleaner::processQueue, "jaloc-cleaner");
        thread.setDaemon(true);
        thread.start();
    }

    private NativeCleaner() {
    }

    /**
     * Registers an allocation with the cleaner.
     *
     * @param state the reachability referent
     * @param record the cleanup payload
     */
    public static void register(AllocationState state, AllocationRecord record) {
        Throwable allocationSite = DEBUG ? new Throwable("Allocation site") : null;

        Registration registration = new Registration(state, record, allocationSite);

        REGISTRATIONS.add(registration);
        record.registration(registration);
    }

    static void unregister(Registration registration) {
        registration.clear();
        REGISTRATIONS.remove(registration);
    }

    private static void processQueue() {
        while (true) {
            Registration registration;

            try {
                registration = (Registration) QUEUE.remove();
            } catch (InterruptedException ignored) {
                continue;
            }

            REGISTRATIONS.remove(registration);

            if (registration.record.close()) {
                reportLeak(registration);
            }
        }
    }

    private static void reportLeak(Registration registration) {
        AllocationRecord record = registration.record;

        StringBuilder message = new StringBuilder()
                .append("Jaloc: leaked native allocation of ")
                .append(record.size())
                .append(" bytes (alignment ")
                .append(record.alignment())
                .append("); memory was freed by the cleaner.");

        if (registration.allocationSite == null) {
            message.append(" Set -Djaloc.debug=true to capture allocation sites.");
        }

        System.err.println(message);

        if (registration.allocationSite != null) {
            registration.allocationSite.printStackTrace();
        }
    }

    static final class Registration extends PhantomReference<AllocationState>
    {
        private final AllocationRecord record;
        private final Throwable allocationSite;

        private Registration(AllocationState state, AllocationRecord record, Throwable allocationSite) {
            super(state, QUEUE);
            this.record = record;
            this.allocationSite = allocationSite;
        }
    }
}
