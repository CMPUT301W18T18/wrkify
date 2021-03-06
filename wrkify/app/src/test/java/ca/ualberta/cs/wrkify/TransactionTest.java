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

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by peter on 28/03/18.
 */

public class TransactionTest {
    private static CachingClient<MockRemoteClient> rc;

    @BeforeClass
    public static void setup() {
        rc= new CachingClient<>(new MockRemoteClient());
    }

    @Test
    public void testTransaction() {
        SimpleRemoteObject sro = new SimpleRemoteObject(5);
        SimpleRemoteObject sro2 = new SimpleRemoteObject(6);
        StateChangeTransaction<SimpleRemoteObject> t = new SimpleTransaction1(sro);
        StateChangeTransaction<SimpleRemoteObject> t2 = new SimpleTransaction2(sro2);

        Boolean status;

        status = t.applyTo(sro);
        assertTrue(status);
        assertEquals(1, sro.field);

        status = t2.applyTo(sro);
        assertFalse(status);
        assertEquals(1, sro.field);

        status = t2.applyTo(sro2);
        assertTrue(status);
        assertEquals(2, sro2.field);

        status = t.applyTo(sro2);
        assertFalse(status);
        assertEquals(2, sro2.field);
    }

    @Test
    public void testTransactionManager() {
        SimpleRemoteObject sro = rc.create(SimpleRemoteObject.class, 5);
        TransactionManager tm = new TransactionManager();
        tm.enqueue(new SimpleTransaction1(sro));
        tm.enqueue(new SimpleTransaction2(sro));

        tm.flush(rc);

        try {
            sro = rc.download(sro.getId(), SimpleRemoteObject.class);
        } catch (IOException e) {
            fail();
        }

        assertEquals(2, sro.field);
    }
}
