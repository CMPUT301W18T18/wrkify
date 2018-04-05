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

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Session is a global singleton that holds a reference
 * to the user that is currently logged in. It automatically
 * saves and restores a user identity from file when the app
 * is restarted.
 */
public class Session {
    private static final String FILENAME = "ca.ualberta.cs.wrkify.Session";
    private static Session instance;

    private User user;
    private List<RemoteReference<Task>> userRequestedCache;
    private List<RemoteReference<Task>> userProvidedCache;
    private List<RemoteReference<Task>> userBiddedCache;

    private TransactionManager transactionManager = new TransactionManager();

    protected Session() {}

    /**
     * Gets the global Session.
     * @param context application context; used to restore a preserved Session
     * @param client RemoteClient to load user data from
     * @return global Session object
     */
    public static Session getInstance(Context context, RemoteClient client) {
        if (instance == null) {
            instance = new Session();
        }

        if (instance.getUser() == null) {
            instance.load(context, client);
        }

        return instance;
    }

    /**
     * Gets the global Session using the default WrkifyClient.
     * @param context application context; used to restore a preserved Session
     * @return global Session object
     */
    public static Session getInstance(Context context) {
        return getInstance(context, WrkifyClient.getInstance());
    }

    /**
     * Override the Session instance.
     * @param instance new Session instance
     */
    public static void setInstance(Session instance) {
        Session.instance = instance;
    }

    /**
     * Gets the logged-in user.
     * @return logged-in user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Sets the logged-in user.
     * @param user User to set as the session user
     * @param context application context; used to save Session
     */
    public void setUser(User user, Context context) {
        this.user = user;
        save(context);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Unsets the logged-in user.
     * @param context application context; used to clear saved Session
     */
    public void logout(Context context) {
        this.user = null;
        context.deleteFile(FILENAME);
    }

    /**
     * Refreshes user task caches.
     * @param client RemoteClient to find user's tasks in
     * @throws IOException if network is disconnected
     */
    public void refreshCaches(RemoteClient client) throws IOException {
        this.userProvidedCache = referenceTaskList(Searcher.findTasksByProvider(client, getUser()));
        this.userRequestedCache = referenceTaskList(Searcher.findTasksByRequester(client, getUser()));
        this.userBiddedCache = referenceTaskList(Searcher.findTasksByBidder(client, getUser()));
    }

    /**
     * Gets the cache of the user's requested tasks.
     * @return cached list of requested tasks
     */
    public List<Task> getUserRequestedCache(RemoteClient client) {
        return resolveCacheList(client, userRequestedCache);
    }

    /**
     * Gets the cache of the user's provided tasks.
     * @return cached list of provided tasks
     */
    public List<Task> getUserProvidedCache(RemoteClient client) {
        return resolveCacheList(client, userProvidedCache);
    }

    /**
     * Gets the cache of the user's bidded tasks.
     * @return cached list of bidded tasks
     */
    public List<Task> getUserBiddedCache(RemoteClient client) {
        return resolveCacheList(client, userBiddedCache);
    }

    private List<Task> resolveCacheList(RemoteClient client, List<RemoteReference<Task>> cacheList) {
        List<Task> tasks = new ArrayList<>();
        for (RemoteReference<Task> reference: cacheList) {
            try {
                tasks.add(reference.getRemote(client, Task.class));
            } catch (IOException e) {
                // continue
            }
        }
        return tasks;
    }

    private List<RemoteReference<Task>> referenceTaskList(List<Task> objectList) {
        List<RemoteReference<Task>> references = new ArrayList<>();
        for (Task task: objectList) {
            references.add(task.<Task>reference());
        }
        return references;
    }

    /**
     * Preserves Session to file
     * @param context application context; used to write file
     */
    private void save(Context context) {
        // this is adapted from the lab 26-01-2018
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME,
                    Context.MODE_PRIVATE);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));

            Gson gson = new Gson();
            gson.toJson(this.user.getId(), out);

            out.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Loads Session from file and gets user data from RemoteClient
     * @param context application context; used to read file
     * @param client RemoteClient; used to retrieve user data
     */
    private void load(Context context, RemoteClient client) {
        // this is adapted from the lab 26-01-2018
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();
            String id = gson.fromJson(in, String.class);

            this.user = client.download(id, User.class);
        } catch (FileNotFoundException e) {
            this.user = null;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
