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

import java.io.Serializable;


/**
 * RemoteObject is an abstract class for defining
 * behaviors of objects that are uploaded to RemoteClients
 */

public abstract class RemoteObject implements Serializable {
    private String id;

    /**
     * sets the id of the RemoteObject.
     * this should only be used by RemoteClient
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * gets the id of the object
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * reuploads the object.
     */
    public void upload(RemoteClient client) {
        client.upload(this);
    }
    
    public <T extends RemoteObject> RemoteReference<T> reference() {
        return new RemoteReference(id, this.getClass());
    }
}
