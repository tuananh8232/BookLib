package luubieunghi.lbn.booklib.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import luubieunghi.lbn.booklib.R;

public class HighlightNoteFrm extends Fragment {

    public HighlightNoteFrm() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_highlight_note, container, false);
    }
}
