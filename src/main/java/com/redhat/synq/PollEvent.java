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

import java.util.concurrent.TimeUnit;

public interface PollEvent<T> extends Event<T> {
    PollEvent<T> pollingEvery(long pollingInterval, TimeUnit pollingUnit);
    PollEvent<T> ignoring(Class<? extends Exception> exception);
    
    @Override
    default PollEvent<T> after(Runnable action) {
        return new SequentialEventWithPollEvent<>((t, u) -> {action.run(); return null;}, this);
    }
    
}