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

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * unit tests for the Bid class
 *
 * @author Tao Huang
 * @see Bid
 */
public class BidTest {
    private static final MockRemoteClient remoteClient = new MockRemoteClient();

    @Test
    public void testBid() throws Exception {
        Price value = new Price(123.45);
        User bidder = remoteClient.create(User.class, "A", "A@example.com", "(555) 555-555");

        Bid user = new Bid(value, bidder);
        Price resultvalue = user.getValue();
        User resultbidder = user.getRemoteBidder(remoteClient);

        assertEquals(resultvalue, value);
        assertEquals(resultbidder, bidder);

    }

    @Test
    public void testGettersAndSetters() throws Exception {
        Price A_value = new Price(123.45);
        Price B_value = new Price(456.78);
        Price C_value = new Price(0.00);

        User A_bidder = remoteClient.create(User.class, "ABC", "ABC@example.com", "(555) 555-555");
        User B_bidder = remoteClient.create(User.class, "CDE", "CDE@example.com", "(666) 666-666");

        Bid user = new Bid(A_value, A_bidder);
        user.setValue(B_value);
        assertEquals(user.getValue(), B_value);

        user.setBidder(B_bidder);
        assertEquals(user.getRemoteBidder(remoteClient), B_bidder);

    }
}