package se.ju.ralu18pz.petfinder;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View window;
    private Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void rendoWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        String[] snippet = marker.getSnippet().split("#", 2);

        TextView titleText = view.findViewById(R.id.window_title);
        TextView dateText = view.findViewById(R.id.window_date);
        ImageView img = view.findViewById(R.id.window_image);

        titleText.setText(title);

        String date = snippet[0];
        String image = snippet[1];

        dateText.setText(date);

        Picasso.with(context).load(Uri.parse(image))
                .fit()
                .centerCrop()
                .into(img);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendoWindowText(marker, window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendoWindowText(marker, window);
        return window;
    }

}
