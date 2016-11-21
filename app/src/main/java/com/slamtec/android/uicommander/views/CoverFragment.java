package com.slamtec.android.uicommander.views;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slamtec.android.uicommander.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoverFragment extends Fragment {


    public CoverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cover, container, false);
    }


}
