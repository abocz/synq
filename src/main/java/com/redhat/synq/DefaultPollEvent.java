/*
 Copyright 2014 Red Hat, Inc. and/or its affiliates.

 This file is part of synq.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.redhat.synq;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultPollEvent<T> implements PollEvent<T> {
    private final Condition<T> condition;
    private long pollingInterval = 1;
    private TimeUnit pollingUnit = TimeUnit.SECONDS;
    private Set<Class<? extends Exception>> ignoredExceptions = new HashSet<>();
    
    public DefaultPollEvent(Condition<T> condition) {
        this.condition = condition;
    }
    
    public DefaultPollEvent<T> pollingEvery(long pollingInterval, TimeUnit pollingUnit) {
        this.pollingInterval = pollingInterval;
        this.pollingUnit = pollingUnit;
        
        return this;
    }
    
    public DefaultPollEvent<T> ignoring(Class<? extends Exception> exception) {
        ignoredExceptions.add(exception);
        return this;
    }
    
    @Override
    public T waitUpTo(long timeout, TimeUnit unit) {
        boolean met = false;
        T lastResult = null;
        long timeoutTime = now() + MILLISECONDS.convert(timeout, unit);
        
        while (!met) {
            try {
                met = condition.isMet();
                lastResult = condition.lastResult();
                
                if (met) {
                    break;
                }
            } catch (Exception e) {
                throwIfNotIgnored(e);
            }
            
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            
            if (now() >= timeoutTime) {
                throw new TimeoutException(this);
            }
            
            try {
                Thread.sleep(MILLISECONDS.convert(pollingInterval, pollingUnit));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        
        return lastResult;
    }
    
    private void throwIfNotIgnored(Throwable t) throws RuntimeException {
        for (Class<? extends Exception> ignoredException : ignoredExceptions) {
            // The getCause check may not be necessary
            if (ignoredException.isInstance(t) || ignoredException.isInstance(t.getCause())) {
                return;
            }
        }
        
        ThrowableUtil.throwUnchecked(t);
    }
    
    private long now() {
        return System.currentTimeMillis();
    }
    
}
