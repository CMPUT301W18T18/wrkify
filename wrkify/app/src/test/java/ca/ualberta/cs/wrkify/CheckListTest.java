/* Copyright 2018 CMPUT301W18T18
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
 */
package ca.ualberta.cs.wrkify;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Created by peter on 23/02/18.
 */

public class CheckListTest {
    @Test
    public void testGetItem() {
        ArrayList<String> items = new ArrayList<String>();

        items.add("zero");
        items.add("one");
        items.add("two");
        items.add("three");

        CheckList check = new CheckList(items);

        assertEquals(check.getItem(0), "zero");
        assertEquals(check.getItem(1), "one");
        assertEquals(check.getItem(2), "two");
        assertEquals(check.getItem(3), "three");
    }

    @Test
    public void testGetSetStatus() {
        ArrayList<String> items = new ArrayList<String>();

        items.add("zero");
        items.add("one");
        items.add("two");
        items.add("three");

        CheckList check = new CheckList(items);

        check.setStatus(0, true);
        check.setStatus(2, true);
        check.setStatus(2, false);
        check.setStatus(3, true);

        assertTrue(check.getStatus(0));
        assertFalse(check.getStatus(1));
        assertFalse(check.getStatus(2));
        assertTrue(check.getStatus(3));

    }

    @Test
    public void testSetItem() {
        ArrayList<String> items = new ArrayList<String>();

        items.add("zero");
        items.add("one");
        items.add("two");
        items.add("three");

        CheckList check = new CheckList(items);

        check.setItem(0, "not zero");
        check.setItem(1, "not one");

        assertEquals(check.getItem(0), "not zero");
        assertEquals(check.getItem(1), "not one");
        assertEquals(check.getItem(2), "two");
        assertEquals(check.getItem(3), "three");
    }
}
