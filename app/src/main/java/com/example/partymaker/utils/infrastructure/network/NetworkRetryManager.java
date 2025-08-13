package com.example.partymaker.utils.infrastructure.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages network retry logic with exponential backoff and circuit breaker pattern.
 * Provides resilient network operations with automatic retry capabilities.
 */
public class NetworkRetryManager {
    private static final String TAG = "NetworkRetryManager";
    
    // Retry configuration
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000L; // 1 second
    private static final long MAX_DELAY_MS = 30000L; // 30 seconds
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final double JITTER_FACTOR = 0.1; // 10% jitter
    
    // Circuit breaker configuration
    private static final int FAILURE_THRESHOLD = 5;
    private static final long CIRCUIT_RESET_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1);
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitOpenTime = 0;
    private volatile boolean circuitOpen = false;
    
    /**
     * Retry configuration class
     */
    public static class RetryConfig {
        public final int maxRetries;
        public final long initialDelayMs;
        public final long maxDelayMs;
        public final double backoffMultiplier;
        public final boolean useExponentialBackoff;
        public final boolean useJitter;
        
        private RetryConfig(Builder builder) {
            this.maxRetries = builder.maxRetries;
            this.initialDelayMs = builder.initialDelayMs;
            this.maxDelayMs = builder.maxDelayMs;
            this.backoffMultiplier = builder.backoffMultiplier;
            this.useExponentialBackoff = builder.useExponentialBackoff;
            this.useJitter = builder.useJitter;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int maxRetries = DEFAULT_MAX_RETRIES;
            private long initialDelayMs = INITIAL_DELAY_MS;
            private long maxDelayMs = MAX_DELAY_MS;
            private double backoffMultiplier = BACKOFF_MULTIPLIER;
            private boolean useExponentialBackoff = true;
            private boolean useJitter = true;
            
            public Builder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }
            
            public Builder initialDelayMs(long initialDelayMs) {
                this.initialDelayMs = initialDelayMs;
                return this;
            }
            
            public Builder maxDelayMs(long maxDelayMs) {
                this.maxDelayMs = maxDelayMs;
                return this;
            }
            
            public Builder backoffMultiplier(double backoffMultiplier) {
                this.backoffMultiplier = backoffMultiplier;
                return this;
            }
            
            public Builder useExponentialBackoff(boolean useExponentialBackoff) {
                this.useExponentialBackoff = useExponentialBackoff;
                return this;
            }
            
            public Builder useJitter(boolean useJitter) {
                this.useJitter = useJitter;
                return this;
            }
            
            public RetryConfig build() {
                return new RetryConfig(this);
            }
        }
    }
    
    /**
     * Retry callback interface
     */
    public interface RetryCallback<T> {
        T execute() throws Exception;
    }
    
    /**
     * Retry result callback interface
     */
    public interface RetryResultCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception exception);
    }
    
    /**
     * Executes an operation with retry logic using default configuration
     */
    public <T> void executeWithRetry(
            @NonNull RetryCallback<T> operation,
            @NonNull RetryResultCallback<T> callback) {
        executeWithRetry(operation, callback, RetryConfig.builder().build());
    }
    
    /**
     * Executes an operation with retry logic using custom configuration
     */
    public <T> void executeWithRetry(
            @NonNull RetryCallback<T> operation,
            @NonNull RetryResultCallback<T> callback,
            @NonNull RetryConfig config) {
        
        // Check circuit breaker
        if (isCircuitOpen()) {
            callback.onFailure(new Exception("Circuit breaker is open - too many recent failures"));
            return;
        }
        
        executeWithRetryInternal(operation, callback, config, 0);
    }
    
    /**
     * Internal retry execution logic
     */
    private <T> void executeWithRetryInternal(
            @NonNull RetryCallback<T> operation,
            @NonNull RetryResultCallback<T> callback,
            @NonNull RetryConfig config,
            int attemptNumber) {
        
        try {
            // Try to execute the operation
            T result = operation.execute();
            
            // Success - reset failure counter and close circuit
            onOperationSuccess();
            callback.onSuccess(result);
            
        } catch (Exception e) {
            // Operation failed
            Log.w(TAG, "Operation failed on attempt " + (attemptNumber + 1), e);
            
            if (attemptNumber < config.maxRetries) {
                // Calculate delay for next retry
                long delay = calculateDelay(attemptNumber, config);
                
                Log.d(TAG, "Retrying in " + delay + "ms (attempt " + (attemptNumber + 2) + "/" + (config.maxRetries + 1) + ")");
                
                // Schedule retry
                handler.postDelayed(() -> {
                    executeWithRetryInternal(operation, callback, config, attemptNumber + 1);
                }, delay);
                
            } else {
                // Max retries reached
                onOperationFailure();
                callback.onFailure(new Exception("Max retries (" + config.maxRetries + ") exceeded", e));
            }
        }
    }
    
    /**
     * Calculates delay for next retry attempt
     */
    private long calculateDelay(int attemptNumber, RetryConfig config) {
        long delay;
        
        if (config.useExponentialBackoff) {
            // Exponential backoff
            delay = (long) (config.initialDelayMs * Math.pow(config.backoffMultiplier, attemptNumber));
        } else {
            // Linear backoff
            delay = config.initialDelayMs * (attemptNumber + 1);
        }
        
        // Apply max delay cap
        delay = Math.min(delay, config.maxDelayMs);
        
        // Add jitter to prevent thundering herd
        if (config.useJitter) {
            double jitter = (Math.random() - 0.5) * 2 * JITTER_FACTOR;
            delay = (long) (delay * (1 + jitter));
        }
        
        return delay;
    }
    
    /**
     * Handles successful operation
     */
    private void onOperationSuccess() {
        consecutiveFailures.set(0);
        if (circuitOpen) {
            circuitOpen = false;
            Log.i(TAG, "Circuit breaker closed after successful operation");
        }
    }
    
    /**
     * Handles failed operation
     */
    private void onOperationFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        
        if (failures >= FAILURE_THRESHOLD && !circuitOpen) {
            openCircuit();
        }
    }
    
    /**
     * Opens the circuit breaker
     */
    private void openCircuit() {
        circuitOpen = true;
        circuitOpenTime = System.currentTimeMillis();
        
        Log.w(TAG, "Circuit breaker opened due to " + FAILURE_THRESHOLD + " consecutive failures");
        
        // Schedule circuit reset
        handler.postDelayed(this::attemptCircuitReset, CIRCUIT_RESET_TIMEOUT_MS);
    }
    
    /**
     * Attempts to reset the circuit breaker
     */
    private void attemptCircuitReset() {
        if (circuitOpen) {
            Log.i(TAG, "Attempting to reset circuit breaker");
            consecutiveFailures.set(0);
            circuitOpen = false;
        }
    }
    
    /**
     * Checks if circuit breaker is open
     */
    private boolean isCircuitOpen() {
        if (circuitOpen) {
            // Check if enough time has passed to attempt reset
            long timeSinceOpen = System.currentTimeMillis() - circuitOpenTime;
            if (timeSinceOpen > CIRCUIT_RESET_TIMEOUT_MS) {
                attemptCircuitReset();
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Gets circuit breaker status
     */
    public String getCircuitStatus() {
        if (circuitOpen) {
            long timeRemaining = CIRCUIT_RESET_TIMEOUT_MS - (System.currentTimeMillis() - circuitOpenTime);
            return "OPEN (resets in " + TimeUnit.MILLISECONDS.toSeconds(timeRemaining) + "s)";
        }
        return "CLOSED (failures: " + consecutiveFailures.get() + "/" + FAILURE_THRESHOLD + ")";
    }
    
    /**
     * Manually resets the circuit breaker
     */
    public void resetCircuit() {
        consecutiveFailures.set(0);
        circuitOpen = false;
        Log.i(TAG, "Circuit breaker manually reset");
    }
    
    /**
     * Cancels all pending retries
     */
    public void cancelAllRetries() {
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "All pending retries cancelled");
    }
}