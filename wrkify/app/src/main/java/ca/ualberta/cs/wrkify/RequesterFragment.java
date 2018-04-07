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

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RequesterFragment displays the lists of task that a task requester needs to see
 * this Fragment is displayed by MainActivity
 *
 * @see MainActivity
 */
public class RequesterFragment extends TasksOverviewFragment {
    private static final int REQUEST_NEW_TASK = 13;

    private RequesterFragmentPagerAdapter adapter;

    /**
     * get the title of the fragment
     * so TasksOverview fragment can display it
     * @return the fragment title
     */
    @Override
    protected String getAppBarTitle() {
        return "My posts";
    }

    /**
     * indicates whether we dispaly the add button for the
     * TaskListFragment at index
     * @param index Index of the current tab
     * @return true when we are on the requested tab
     */
    @Override
    protected boolean isAddButtonEnabled(int index) {
        return (index == 0);
    }

    /**
     * starts the edit task activity wehn the add button is clicked
     * @param v View corresponding to the add button
     */
    @Override
    protected void onAddButtonClick(View v) {
        Intent newTaskIntent = new Intent(getContext(), EditTaskActivity.class);
        startActivityForResult(newTaskIntent, REQUEST_NEW_TASK);
    }

    /**
     * refresh the reuqested taskList when we return from editing it
     * @param requestCode the android RequestCode
     * @param resultCode the android ResultCode
     * @param data the intent associated with the result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // update the list
        this.adapter.refreshRequested();
    }

    /**
     * return the FragmentPagerAdapter that this Fragment uses
     * @param fragmentManager the fragmentmanger needed FragmentPagerAdapter
     * @return the FragmentPagerAdapter
     */
    @Override
    protected FragmentPagerAdapter getFragmentPagerAdapter(FragmentManager fragmentManager) {
        this.adapter = new RequesterFragmentPagerAdapter(fragmentManager);
        return this.adapter;
    }

    /**
     * RequesterFragmentAdapter is a FragmentPagerAdapter that
     * switches between the Requested, Assigned, and Closed views
     * that a task requester will see.
     */
    static class RequesterFragmentPagerAdapter extends FragmentPagerAdapter {

        private RequestedListFragment requested;

        /**
         * create a RequesterFragmentPagerAdapter
         * @param fragmentManager the fragment manager
         */
        public RequesterFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Log.i("-->", "got item " + position);
            switch (position) {
                case 0:
                    this.requested = new RequestedListFragment();
                    return this.requested;
                case 1:
                    return new AssignedListFragment();
                case 2:
                    return new ClosedListFragment();
                default:
                    return null;
            }
        }

        /**
         * refreshed the requested TaskList
         */
        public void refreshRequested() {
            this.requested.refresh();
        }

        /**
         * gets the title of the page by the position
         * @param position the position
         * @return the title of the page
         */
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return (new CharSequence[]{"Requested", "Assigned", "Closed"})[position];
        }

        /**
         * returns the number of tabs in our tabview.
         * @return the number of tabs.
         */
        @Override
        public int getCount() {
            return 3;
        }
    }

    /**
     * RequestedListFragment is a TaskListFragment based off of
     * tasks that are requested by the session user and may or
     * may not have been bidded on
     *
     * @see TaskListFragment
     */
    public static class RequestedListFragment extends TaskListFragment {
        @Override
        protected RemoteList getTaskList() {
            return new RemoteQueryList<Task>(WrkifyClient.getInstance(), Task.class) {
                @Override
                public List query(RemoteClient client) {
                    try {
                        return Searcher.findTasksByRequester(
                                client,
                                Session.getInstance(getActivity(),client).getUser(),
                                TaskStatus.REQUESTED, TaskStatus.BIDDED
                        );
                    } catch (IOException e) {
                        return null;
                    }
                }
            };
        }
    }

    /**
     * AssignedListFragment is a TaskListFragment based off of
     * tasks that are requested by the session user and have
     * been assigned to a provider
     *
     * @see TaskListFragment
     */
    public static class AssignedListFragment extends TaskListFragment {
        @Override
        protected RemoteList getTaskList() {
            return new RemoteQueryList<Task>(WrkifyClient.getInstance(), Task.class) {
                @Override
                public List query(RemoteClient client) {
                    try {
                        return Searcher.findTasksByRequester(
                                client,
                                Session.getInstance(getActivity(),client).getUser(),
                                TaskStatus.ASSIGNED
                        );
                    } catch (IOException e) {
                        return null;
                    }
                }
            };
        }
    }

    /**
     * ClosedListFragment is a TaskListFragment based off of
     * tasks that are requested by the session user and have
     * been finished
     *
     * @see TaskListFragment
     */
    public static class ClosedListFragment extends TaskListFragment {
        @Override
        protected RemoteList getTaskList() {
            return new RemoteQueryList<Task>(WrkifyClient.getInstance(), Task.class) {
                @Override
                public List query(RemoteClient client) {
                    try {
                        return Searcher.findTasksByRequester(
                                client,
                                Session.getInstance(getActivity(),client).getUser(),
                                TaskStatus.DONE
                        );
                    } catch (IOException e) {
                        return null;
                    }
                }
            };
        }
    }
}
