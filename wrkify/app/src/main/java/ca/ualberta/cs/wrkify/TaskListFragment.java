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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * TaskListFragment is an abstract class that handles the
 * behavior of a List of tasks it defines the behavior
 * of scrolling as well as selecting tasks.
 *
 * @see Fragment
 */
public abstract class TaskListFragment extends Fragment {

    private RemoteList<Task> tasks;
    private TaskListAdapter<Task> taskListAdapter;

    /**
     * create our TaskListFragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tasks = getTaskList();
    }

    /**
     * create the view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("-->", "created view");
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        if (tasks.size() == 0) {
            view.findViewById(R.id.taskListView).setVisibility(View.GONE);
            view.findViewById(R.id.taskListEmptyMessage).setVisibility(View.VISIBLE);
        }

        this.taskListAdapter = new TaskListAdapter<Task>(getContext(), tasks);
        RecyclerView recyclerView = view.findViewById(R.id.taskListView);
        recyclerView.setAdapter(taskListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    /**
     * refresh the view when the you go aaway and look back.
     */
    @Override
    public void onResume() {
        super.onResume();

        this.refresh();
        this.taskListAdapter.notifyDataSetChanged();
        if (this.getView() != null) {
            this.getView().findViewById(R.id.taskListView).setVisibility(tasks.size() == 0? View.GONE : View.VISIBLE);
            this.getView().findViewById(R.id.taskListEmptyMessage).setVisibility(tasks.size() == 0? View.VISIBLE : View.GONE);
        }
    }

    /**
     * refresh the view
     */
    protected void refresh() {
        this.tasks.refresh();
        this.taskListAdapter.notifyDataSetChanged();
    }

    /**
     * allow the subclass to define the creation of the TaskList
     * @return
     */
    protected abstract RemoteList getTaskList();
}
