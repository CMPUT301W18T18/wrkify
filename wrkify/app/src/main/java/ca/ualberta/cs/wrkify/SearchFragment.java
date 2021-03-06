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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchFragment allows users to search for tasks
 * this Fragment is displayed by MainActivity
 * Uses Searcher for search queries and TaskListAdapter
 * for usage in a RecyclerView of tasks. Selecting a
 * task from the search results list launches a ViewTaskActivity
 *
 * @see MainActivity
 * @see ViewTaskActivity
 * @see TaskListAdapter
 */
public class SearchFragment extends Fragment {
    // From https://developer.android.com/guide/components/fragments.html (2018-03-11)
    private List<Task> taskList = new ArrayList<>();
    private TaskListAdapter<Task> adapter;
    private RecyclerView recycler;

    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates the fragment view. And sets search bar
     * 'on submit' method.
     *
     * @param inflater Parent inflater to be inflated
     * @param container Parent ViewGroup, root of fragment
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.taskSearchRecycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        Context context = getContext();
        TaskListAdapter<Task> ad = new TaskListAdapter<Task>(context,this.taskList);
        recyclerView.setAdapter(ad);
        this.adapter = ad;
        this.recycler = recyclerView;

        final EditText searchBar = (EditText) rootView.findViewById(R.id.searchBar);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SearchFragment.this.query = searchBar.getText().toString();
                    searchTasks(SearchFragment.this.query);
                    handled = true;
                }
                return handled;
            }
        });

        // Make search results dodge search bar
        searchBar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                int marginTotal = params.topMargin + params.bottomMargin;

                recycler.setPadding(0, bottom - top + marginTotal, 0, 0);
            }
        });

        final TextView mapIcon = (TextView) rootView.findViewById(R.id.nearme);
        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMap();
            }
        });

        return rootView;
    }


    /**
     * Updates the list of tasks to be displayed according
     * to a search query. Uses Searcher for searching.
     *
     * @param query String search query.
     */
    public void searchTasks(String query){
        this.new SearchTask().execute(query);
    }


    /**
     * Safely updates TaskListAdapter to data changes
     * that occured as a result of a search.
     *
     * @param newList List of tasks returned by a keyword search
     */
    public void updateDataSet(List<Task> newList){

        this.taskList = newList;
        TaskListAdapter<Task> newAdapter = new TaskListAdapter<>(new AppCompatActivity(),this.taskList);
        this.recycler.setAdapter(newAdapter);
    }


    public void viewMap() {
        Intent intent = new Intent(this.getContext(), TasksNearMeMap.class);
        startActivity(intent);
    }

    /**
     * refresh the search results when
     * the activity resumes.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (this.query != null) {
            searchTasks(this.query);
        }
    }

    /**
     * SearchTask is an AsyncTask that preforms a search and then
     * updates the tasklist.
     */
    private class SearchTask extends AsyncTask<String, Void, List<Task>> {
        /**
         * preforms the search
         * @param strings a single string that is the query.
         * @return the list of tasks from the query.
         */
        @Override
        protected List<Task> doInBackground(String... strings) {
            if (strings.length != 1) {
                throw new IllegalArgumentException("exactly one query must be provided");
            }

            String query = strings[0];

            List<Task> tasks;
            try {
                tasks = WrkifyClient.getInstance().getSearcher().findTasksByKeywords(query);
            } catch (IOException e){
                tasks = new ArrayList<>();
            }

            if(tasks==null){
                tasks = new ArrayList<>();
            }

            return tasks;
        }

        /**
         * after the tasks have been fetched,
         * update the data set
         * @param tasks the tasks of our search
         */
        @Override
        protected void onPostExecute(List<Task> tasks) {
            updateDataSet(tasks);
        }
    }
}
