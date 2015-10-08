package schule.adrian.montagsmaler;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Platzhalter-Fragment mit Layout-Elementen für die LobbyDetailActivity
 */
public class LobbyDetailActivityFragment extends Fragment {

    public LobbyDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lobby_detail, container, false);
    }
}
