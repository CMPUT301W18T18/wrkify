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


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.io.IOException;
import java.util.List;

/**
 * Collection of static methods to perform pre-defined searches.
 */
public class Searcher {

    /**
     * Find all tasks where the given User is the task requester.
     * @param client RemoteClient to search in
     * @param requester User to search for
     * @return List of tasks matching the search (may be empty)
     * @throws IOException if RemoteClient is disconnected
     */
    static List<Task> findTasksByRequester(RemoteClient client, User requester) throws IOException {
        String query = "{\"query\":{\"nested\":{\"path\":\"requester\",\"query\":"
                + "{\"match\":{\"requester.refId\":\"" + requester.getId() + "\"}}}}}";
        return client.search(query, Task.class);
    }

    /**
     * Find all tasks where the given User is the assigned task provider.
     * @param client RemoteClient to search in
     * @param provider User to search for
     * @return List of tasks matching the search (may be empty)
     * @throws IOException if RemoteClient is disconnected
     */
    static List<Task> findTasksByProvider(RemoteClient client, User provider) throws IOException {
        String query = "{\"query\":{\"nested\":{\"path\":\"provider\",\"query\":"
                + "{\"match\":{\"provider.refId\":\"" + provider.getId() + "\"}}}}}";
        return client.search(query, Task.class);
    }

    /**
     * Find all tasks where the given User has placed a bid.
     * @param client RemoteClient to search in
     * @param bidder User to search for
     * @return List of tasks matching the search (may be empty)
     * @throws IOException if RemoteClient is disconnected
     */
    static List<Task> findTasksByBidder(RemoteClient client, User bidder) throws IOException {
        String query = "{\"query\":{\"nested\":{\"path\": \"bidList.bidder\",\"query\":"
                +"{\"match\":{\"bidList.bidder.refId\": \"" + bidder.getId() + "\"}}}}}";
        return client.search(query, Task.class);
    }

    /**
     * Find all tasks matching a search string.
     * @param client RemoteClient to search in
     * @param keywords Keywords to search for
     * @return List of tasks matching the search (may be empty)
     * @throws IOException if RemoteClient is disconnected
     */
    static List<Task> findTasksByKeywords(RemoteClient client, String keywords) throws IOException {
     // from https://stackoverflow.com/questions/7899525/ (2018-03-18)
        String[] splited = keywords.split("\\s+");

        // from http://www.appsdeveloperblog.com/java-into-json-json-into-java-all-possible-examples/ (2018-03-18)
        Gson gsonBuilder = new GsonBuilder().create();
        String json = gsonBuilder.toJson(splited);

        String query = String.format(
                "{\"query\":{\"bool\":{\"should\":[{\"terms\":{\"title\":%s}},"
                        +"{\"terms\":{\"description\":%s}}]}}}",
                json, json);
        return client.search(query, Task.class);
    }

    /**
     * gets a user by its username
     * @param client RemoteClient to search in
     * @param username the username of the user
     * @return the User associated with username
     * @throws IOException if RemoteClient is disconnected
     */
    static User getUser(RemoteClient client, String username) throws IOException {
        String query = "{\"query\":{\"match\":{\"username\": \"" + username + "\"}}}";
        List<User> results = client.search(query, User.class);
        if (results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

    // TODO findTasksByLocation?
}
