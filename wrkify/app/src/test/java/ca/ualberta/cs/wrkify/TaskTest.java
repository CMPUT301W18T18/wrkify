package ca.ualberta.cs.wrkify;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * Created by Craig on 2018-02-24.
 *
 * Test Cases fot Task class
 *
 * Modified 2018-02-25
 */

public class TaskTest {
    private static final MockRemoteClient remoteClient = new MockRemoteClient();

    @Test
    public void testFailConstructor() {
        User use = new User("a", "a@a.com", "7");

        boolean error = false;
        try {
            Task concTask = new Task("", use, "");
        } catch (IllegalArgumentException e){
            error = true;
        }
        assertTrue(error);

        error = false;
        try {
            Task concTask = new Task("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh", use, "");
        } catch (IllegalArgumentException e){
            error = true;
        }
        assertTrue(error);

        StringBuffer outputBuffer = new StringBuffer(600);
        for (int i = 0; i < 600; i++){
            outputBuffer.append("a");
        }

        error = false;
        try {
            Task concTask = new Task("hello", use, outputBuffer.toString());
        } catch (IllegalArgumentException e){
            error = true;
        }
        assertTrue(error);

    }

    @Test
    public void testSetGetTitle() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");
        concTask.setTitle("Test Title");

        assertEquals("Test Title", concTask.getTitle());
    }

    @Test
    public void testSetGetDescription() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "def descr");
        concTask.setDescription("Test Description");

        assertEquals("Test Description",concTask.getDescription());
    }

    @Test
    public void testCancelBid() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");

        Bid bid = new Bid(new Price(5.0), use);

        concTask.addBid(bid);
        concTask.cancelBid(bid);

        assertEquals(TaskStatus.REQUESTED, concTask.getStatus());
        assertEquals(0, concTask.getBidList().size());
    }

    @Test
    public void testSetGetImageList() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");

        Bitmap image = null; // Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888); is not mocked
        concTask.addImage(image);

        assertEquals(image, concTask.getImageList().get(0));
    }

    @Test
    public void testSetGetChecklist() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");
        CheckList checklist = new CheckList();

        concTask.setCheckList(checklist);

        assertEquals(checklist, concTask.getCheckList());
    }

    @Test
    public void testAddBid() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");

        Bid bid = new Bid(new Price(5.0), use);
        Bid bid2 = new Bid(new Price(3.0), use);

        concTask.addBid(bid);
        concTask.addBid(bid2);

        ArrayList<Bid> bids = concTask.getBidList();
        assertEquals(2, bids.size());
        assertEquals(bid2, bids.get(0));
        assertEquals(bid, bids.get(1));

        assertEquals(TaskStatus.BIDDED, concTask.getStatus());
    }

    @Test
    public void testSetGetProvider() {
        User concUser = remoteClient.create(User.class, "Test", "Test@Test.com", "12 345 67890");
        Task concTask = new Task("def", concUser, "");


        concTask.setProvider(concUser);

        try {
            assertEquals(concUser, concTask.getRemoteProvider(remoteClient));
        } catch (IOException e) { fail(); }
    }


    @Test
    public void testBidSorting() {
        Random rand = new Random();
        double lowbid = 20.0;
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");

        for(int i=0; i < 10; i++) {
            double number = rand.nextDouble() * 20;
            if (number < lowbid) {
                lowbid = number;
            }
            User bidder = new User("username", "email@example.com", "(555) 555-5555");
            Bid bid = new Bid(new Price(number), bidder);
            concTask.addBid(bid);
        }

        assertEquals(new Price(lowbid), concTask.getBidList().get(0).getValue());
    }

    @Test
    public void testAcceptUnassignBid() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");
        User bidder = new User("username", "email@example.com", "(555) 555-5555");
        Bid bid = new Bid(new Price(20.0), bidder);

        concTask.acceptBid(bid);

        assertEquals(bid.getValue(), concTask.getAcceptedBid().getValue());
        assertEquals(TaskStatus.ASSIGNED, concTask.getStatus());

        concTask.unassign();
        assertEquals(TaskStatus.REQUESTED, concTask.getStatus());
    }

    @Test
    public void testGetPrice() {
        Price price1 = new Price(20.0);
        Price price2 = new Price(10.0);

        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");
        concTask.addBid(new Bid(price1, use));
        concTask.addBid(new Bid(price2, use));

        assertEquals(price2, concTask.getPrice());

        concTask.acceptBid(concTask.getBidList().get(1));

        assertEquals(price1, concTask.getPrice());
    }

    @Test
    public void testComplete() {
        User use = new User("a", "a@a.com", "7");
        Task concTask = new Task("def title", use, "");

        Bid bid = new Bid(new Price(10.0), use);
        concTask.addBid(bid);
        concTask.acceptBid(bid);

        assertEquals(TaskStatus.ASSIGNED, concTask.getStatus());

        concTask.complete();

        assertEquals(TaskStatus.DONE, concTask.getStatus());
    }

}
