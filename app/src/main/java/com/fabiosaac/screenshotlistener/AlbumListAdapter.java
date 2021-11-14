package com.fabiosaac.screenshotlistener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumItemHolder> {
  private final Context context;
  private final ArrayList<String> albums;

  public AlbumListAdapter(Context context) {
    this.context = context;
    this.albums = new ArrayList<>(
      Arrays.asList("New album", "Some Album", "Photos", "Screenshots", "Good food", "photos pro")
    );
  }

  @NonNull
  @Override
  public AlbumItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.album_item, parent, false);

    return new AlbumItemHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AlbumListAdapter.AlbumItemHolder holder, int position) {
    holder.bind(albums.get(position), position);
  }

  @Override
  public int getItemCount() {
    return albums.size();
  }

  public class AlbumItemHolder extends RecyclerView.ViewHolder {
    public final MaterialButton button;

    public AlbumItemHolder(@NonNull View view) {
      super(view);

      button = view.findViewById(R.id.button);
    }

    public void bind(String album, int position) {
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      );

      if (position == 0) {
        layoutParams.leftMargin = (int) (26 * context.getResources().getDisplayMetrics().density);
        button.setLayoutParams(layoutParams);
      } else if (position == (albums.size() - 1)) {
        layoutParams.rightMargin = (int) (26 * context.getResources().getDisplayMetrics().density);
        button.setLayoutParams(layoutParams);
      }

      this.button.setText(album);
    }
  }
}
