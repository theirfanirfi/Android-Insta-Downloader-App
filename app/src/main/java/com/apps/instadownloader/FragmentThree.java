package com.apps.instadownloader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Irfan Ullah on 03/01/2018.
 */

public class FragmentThree extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_howto, container, false);
        ImageView h = (ImageView) rootView.findViewById(R.id.imageHow);
        h.setImageResource(R.drawable.shot3);

        return rootView;
    }
}
