package io.github.requestlog.core.enums;


/**
 * Retry interval calculation strategy.
 */
public enum RetryWaitStrategy {

    /**
     * Fixed interval
     * 60s、60s、60s、60s ...
     */
    FIXED() {
        @Override
        public long nextExecuteTime(long lastExecuteTimeMillis, int currentExecuteCount, int interval) {
            return lastExecuteTimeMillis + interval;
        }
    },

    /**
     * Incremental
     * 60s、120s、180s、240s ...
     */
    INCREMENT() {
        @Override
        public long nextExecuteTime(long lastExecuteTimeMillis, int currentExecuteCount, int interval) {
            return lastExecuteTimeMillis + (long) interval * currentExecuteCount;
        }
    },

    /**
     * Fibonacci
     * 60s、60s、120s、180s、300s ...
     */
    FIBONACCI() {
        @Override
        public long nextExecuteTime(long lastExecuteTimeMillis, int currentExecuteCount, int interval) {
            int increment = 0;
            if (currentExecuteCount <= 2) {
                increment = interval;
            } else {
                int slow = interval, fast = interval + interval;
                for (int i = 3; i < currentExecuteCount; i++) {
                    int fastTemp = fast;
                    fast = fast + slow;
                    slow = fastTemp;
                }
                increment = fast;
            }
            return lastExecuteTimeMillis + increment;
        }

    };

    /**
     * Calculate the next retry time
     *
     * @param lastExecuteTimeMillis Last execution time
     * @param currentExecuteCount   Current retry count (starting from 1)
     * @param interval              Interval, in seconds.
     * @return Next retry time
     */
    public abstract long nextExecuteTime(long lastExecuteTimeMillis, int currentExecuteCount, int interval);

    /**
     * @see RetryWaitStrategy#nextExecuteTime(long, int, int)
     */
    public long nextExecuteTime(int currentExecuteCount, int interval) {
        return this.nextExecuteTime(System.currentTimeMillis(), currentExecuteCount, interval);
    }

}
