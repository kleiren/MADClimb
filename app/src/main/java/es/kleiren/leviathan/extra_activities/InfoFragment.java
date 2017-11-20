/*
 * Copyright 2014 Soichiro Kashima
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

package es.kleiren.leviathan.extra_activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;

import es.kleiren.leviathan.R;

public class InfoFragment extends Fragment {
    String location, name;

    private static final String ARG_LOC = "location";
    private static final String ARG_NAME = "name";


    public static InfoFragment newInstance(String location, String name) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOC, location);
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            location =  getArguments().getString(ARG_LOC);
            name =  getArguments().getString(ARG_NAME);

        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);


        getFragmentManager().beginTransaction()
                .replace(R.id.map_container, MapsFragment.newInstance(location, name))
                .commit();

        final ObservableScrollView scrollView = (ObservableScrollView) view.findViewById(R.id.scroll);
        Activity parentActivity = getActivity();
        scrollView.setTouchInterceptionViewGroup((ViewGroup) parentActivity.findViewById(R.id.container));
        if (parentActivity instanceof ObservableScrollViewCallbacks) {
            scrollView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentActivity);
        }

//        try {
//            ((TextView) view.findViewById(R.id.txtInfo)).setText(new WikipediaParser("en").execute(MainActivity.currentZone.getName()).get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        return view;
    }
}
