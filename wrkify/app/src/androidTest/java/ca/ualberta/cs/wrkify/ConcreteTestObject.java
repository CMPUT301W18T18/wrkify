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

/**
 * ConcreteTestObject is Used for testing ElasticClient
 */

public class ConcreteTestObject extends RemoteObject {
    public final String param1;
    public final String param2;
    public final int param3;

    public ConcreteTestObject(String param1, String param2, Integer param3) {
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }
}