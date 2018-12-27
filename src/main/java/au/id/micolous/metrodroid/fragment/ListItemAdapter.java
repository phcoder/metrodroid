/*
 * ListItemAdapter.java
 *
 * Copyright 2012 Eric Butler <eric@codebutler.com>
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.id.micolous.metrodroid.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import au.id.micolous.metrodroid.ui.HeaderListItem;
import au.id.micolous.metrodroid.ui.ListItem;
import au.id.micolous.metrodroid.ui.UriListItem;

import java.util.List;

import au.id.micolous.farebot.R;

public class ListItemAdapter extends ArrayAdapter<ListItem> {
    public ListItemAdapter(Context context, List<ListItem> items) {
        super(context, 0, items);
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof HeaderListItem) ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            int viewId = getItemViewType(position) == 0 ? R.layout.list_header : android.R.layout.simple_list_item_2;
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(viewId, parent, false);
        }

        ListItem item = getItem(position);

        if (item instanceof HeaderListItem) {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.getText1());
        } else {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.getText1());
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(item.getText2());
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position) instanceof UriListItem;
    }
}
