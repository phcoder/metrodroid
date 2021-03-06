/*
 * HeaderListItem.java
 *
 * Copyright 2012 Eric Butler <eric@codebutler.com>
 * Copyright 2018 Michael Farrell <micolous+git@gmail.com>
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

package au.id.micolous.metrodroid.ui;

import android.support.annotation.StringRes;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import au.id.micolous.farebot.R;

public class HeaderListItem extends ListItem {
    public HeaderListItem(@StringRes int titleResource) {
        super(titleResource);
    }

    public HeaderListItem(String title) {
        super(title);
    }

    public HeaderListItem(Spanned title) {
        super(title);
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        View view = inflater.inflate(R.layout.list_header, root, attachToRoot);

        ((TextView) view.findViewById(android.R.id.text1)).setText(getText1());
        return view;
    }
}
