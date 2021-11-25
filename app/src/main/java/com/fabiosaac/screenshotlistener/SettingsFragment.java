package com.fabiosaac.screenshotlistener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
  private SettingsProvider settingsProvider;

  private View container;
  private SwitchCompat notificationSwitch;
  private SwitchCompat deleteOnShareSwitch;
  private SwitchCompat deleteOnSaveSwitch;
  private SwitchCompat accumulateNotificationsSwitch;
  private RadioGroup notificationAlbumsRadioGroup;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    this.settingsProvider = SettingsProvider.getInstance(this.getContext());
    this.container = inflater.inflate(R.layout.fragment_settings, container, false);

    initializeViews();

    return this.container;
  }

  private void initializeViews() {
    this.notificationSwitch = this.container.findViewById(R.id.notificationSwitch);
    this.deleteOnShareSwitch = this.container.findViewById(R.id.deleteOnShareSwitch);
    this.deleteOnSaveSwitch = this.container.findViewById(R.id.deleteOnSaveSwitch);
    this.accumulateNotificationsSwitch =
      this.container.findViewById(R.id.accumulateNotificationsSwitch);
    this.notificationAlbumsRadioGroup =
      this.container.findViewById(R.id.notificationAlbumsRadioGroup);

    setViewValues();
    setViewListeners();
  }

  private void setViewValues() {
    this.notificationSwitch.setChecked(settingsProvider.getNotificationDisabled());
    this.deleteOnSaveSwitch.setChecked(settingsProvider.getDeleteOnSave());
    this.deleteOnShareSwitch.setChecked(settingsProvider.getDeleteOnShare());
    this.accumulateNotificationsSwitch.setChecked(settingsProvider.getAccumulateNotifications());

    if (SettingsProvider.getInstance(getContext()).getNotificationAlbums().equals("mostUsed")) {
      ((RadioButton) this.notificationAlbumsRadioGroup.findViewById(R.id.mostUsedRadio))
        .setChecked(true);
    } else {
      ((RadioButton) this.notificationAlbumsRadioGroup.findViewById(R.id.recentsRadio))
        .setChecked(true);
    }
  }

  private void setViewListeners() {
    this.notificationSwitch.setOnCheckedChangeListener(
      (view, value) -> settingsProvider.setNotificationDisabled(value));

    this.deleteOnSaveSwitch.setOnCheckedChangeListener(
      (view, value) -> settingsProvider.setDeleteOnSave(value));

    this.deleteOnShareSwitch.setOnCheckedChangeListener(
      (view, value) -> settingsProvider.setDeleteOnShare(value));

    this.accumulateNotificationsSwitch.setOnCheckedChangeListener(
      (view, value) -> settingsProvider.setAccumulateNotifications(value));

    this.notificationAlbumsRadioGroup.setOnCheckedChangeListener((radioGroup, id) ->
      settingsProvider.setNotificationAlbums(id == R.id.mostUsedRadio ? "mostUsed" : "recent"));
  }
}