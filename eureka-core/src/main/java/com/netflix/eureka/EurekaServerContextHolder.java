/*
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.eureka;

/**
 * A static holder for the server context for use in non-DI cases.
 *
 * @author David Liu
 * Eureka-Server 上下文持有者
 */
public class EurekaServerContextHolder {
    /**
     * Eureka-Server 上下文
     */
    private final EurekaServerContext serverContext;

    private EurekaServerContextHolder(EurekaServerContext serverContext) {
        this.serverContext = serverContext;
    }

    public EurekaServerContext getServerContext() {
        return this.serverContext;
    }

    private static EurekaServerContextHolder holder;
    /**
     * 绑定EurekaServerContext
     *
     * @param serverContext Eureka-Server 上下文
     */
    public static synchronized void initialize(EurekaServerContext serverContext) {
        holder = new EurekaServerContextHolder(serverContext);
    }

    public static EurekaServerContextHolder getInstance() {
        return holder;
    }
}
