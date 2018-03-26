/*
 * Copyright 2018 CMPUT301W18T18
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ca.ualberta.cs.wrkify;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by peter on 25/03/18.
 */

public class TransactionProxyHandler<T extends RemoteObject> implements InvocationHandler {
    private TransactionManager manager;
    private T internal;

    public TransactionProxyHandler(TransactionManager tm, T internal) {
        this.manager = tm;
        this.internal = internal;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Transact.class) != null) {
            Transaction<T> transaction = new Transaction<T>(internal, method, args);
            this.manager.enqueue(transaction);
        }
        return method.invoke(internal, args);
    }
}
