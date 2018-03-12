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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BidListAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private ArrayList<String> groups;
    private ArrayList<Integer> children;
    private ExclusiveExpandableListView listView;

    public static int headerLayoutID = R.layout.activity_view_bids_item_header;
    public static int childLayoutID = R.layout.activity_view_bids_item_child;




    public BidListAdapter(Context context, List<String> groups, List<Integer> children, ExclusiveExpandableListView listView) {
        this._context = context;
        this.groups = new ArrayList<String>(groups);
        this.children = new ArrayList<Integer>(children);
        this.listView = listView;
    }


    @Override
    public Object getChild(int groupPos, int childPos) {
        return children.get(childPos);
    }

    @Override
    public long getChildId(int groupPos, int childPos) {
        return childPos;
    }

    @Override
    public int getChildrenCount(int groupPos){
        if (groupPos < groups.size() && groupPos >= 0 && groups.size() > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public View getChildView(int groupPos, final int childPos, boolean isLastChild, View convertView, ViewGroup parent) {
        final int value = (Integer) getChild(groupPos, childPos);

        if (convertView == null) {
            convertView = LayoutInflater.from(_context).inflate(BidListAdapter.childLayoutID, null);
        }

        final Button button1 = (Button) convertView.findViewById(R.id.bidListChildButton);
        final Button button2 = (Button) convertView.findViewById(R.id.bidListChildButton2);
        final TextView textView = (TextView) convertView.findViewById(R.id.bidListChildTextView);

        button1.setText("Increase");
        button2.setText("Delete");
        textView.setText(Integer.toString((Integer)getChild(groupPos, childPos)));

        return convertView;
    }



    @Override
    public Object getGroup(int groupPos) {
        return groups.get(groupPos);
    }

    @Override
    public long getGroupId(int groupPos) {
        return groupPos;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }


    @Override
    public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
        String title = (String)getGroup(groupPos);

        if (convertView == null) {
            convertView = LayoutInflater.from(_context).inflate(BidListAdapter.headerLayoutID, null);
        }

        final TextView textView = (TextView) convertView.findViewById(R.id.bidListHeaderText);

        textView.setText(title);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPos, int childPos) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


}
