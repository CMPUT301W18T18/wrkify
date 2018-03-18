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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created by peter on 18/03/18.
 */

public class SearcherTest {

    private static User user1;
    private static User user2;
    private static User user3;

    private static Task task1;
    private static Task task2;
    private static Task task3;
    private static Task task4;

    private static RemoteClient rc;

    @BeforeClass
    public static void createData() {
        rc = WrkifyClient.getInstance();

        user1 = rc.create(User.class, "peter", "peter@a.com", "1");
        user2 = rc.create(User.class, "taylor", "taylor@a.com", "2");
        user3 = rc.create(User.class, "john", "john@a.com", "3");

        task1 = rc.create(Task.class, "task 1", user1, "");
        task2 = rc.create(Task.class, "task 2", user1, "");
        task3 = rc.create(Task.class, "task 3", user2, "");
        task4 = rc.create(Task.class, "task 4", user2, "");

        task2.acceptBid(new Bid(1.0, user3));
        rc.upload(task2);

        task4.addBid(new Bid(1.0, user3));
        rc.upload(task4);

        // give elasticsearch time to index
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
    }

    @AfterClass
    public static void removeData() {
        rc.delete(user1);
        rc.delete(user2);
        rc.delete(user3);

        rc.delete(task1);
        rc.delete(task2);
        rc.delete(task3);
        rc.delete(task4);
    }

    @Test
    public void testFindTasksByRequester() {

        List<Task> res1;
        try {
            res1 = Searcher.findTasksByRequester(rc, user1);
        } catch (IOException e) {
            fail();
            return;
        }

        assertEquals(2, res1.size());
        assertTrue(res1.contains(task1));
        assertTrue(res1.contains(task2));

        List<Task> res3;
        try {
            res3 = Searcher.findTasksByRequester(rc, user3);
        } catch (IOException e) {
            fail();
            return;
        }

        assertEquals(0, res3.size());
    }

    @Test
    public void testfindTasksByProvider() {
        List<Task> res2;
        try {
            res2 = Searcher.findTasksByProvider(rc, user3);
        } catch (IOException e) {
            fail();
            return;
        }

        assertEquals(1, res2.size());
        assertTrue(res2.contains(task2));
    }

    @Test
    public void testfindTasksByBidder() {
        List<Task> res3;
        try {
            res3 = Searcher.findTasksByBidder(rc, user3);
        } catch (IOException e) {
            fail();
            return;
        }

        assertEquals(1, res3.size());
        assertTrue(res3.contains(task4));

        List<Task> res1;
        try {
            res1 = Searcher.findTasksByBidder(rc, user1);
        } catch (IOException e) {
            fail();
            return;
        }

        assertEquals(0, res1.size());
    }
}