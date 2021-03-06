/***
  Copyright (c) 2013 CommonsWare, LLC
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may
  not use this file except in compliance with the License. You may obtain
  a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.commonsware.cwac.camera.acl.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.commonsware.cwac.camera.acl.CameraFragment;

public class DemoCameraFragment extends CameraFragment {
  private static final String KEY_USE_FFC=
      "com.commonsware.cwac.camera.demo.USE_FFC";
  private MenuItem singleShotItem=null;
  private MenuItem autoFocusItem=null;
  private MenuItem takePictureItem=null;
  private boolean singleShotProcessing=false;

  static DemoCameraFragment newInstance(boolean useFFC) {
    DemoCameraFragment f=new DemoCameraFragment();
    Bundle args=new Bundle();

    args.putBoolean(KEY_USE_FFC, useFFC);
    f.setArguments(args);

    return(f);
  }

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);

    setHasOptionsMenu(true);
    setHost(new DemoCameraHost(getActivity()));
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.camera, menu);
    takePictureItem=menu.findItem(R.id.camera);
    singleShotItem=menu.findItem(R.id.single_shot);
    singleShotItem.setChecked(getContract().isSingleShotMode());
    autoFocusItem=menu.findItem(R.id.autofocus);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.camera:
        if (singleShotItem.isChecked()) {
          singleShotProcessing=true;
          takePictureItem.setEnabled(false);
        }

        takePicture();

        return(true);

      case R.id.autofocus:
        autoFocus();

        return(true);

      case R.id.single_shot:
        item.setChecked(!item.isChecked());
        getContract().setSingleShotMode(item.isChecked());

        return(true);
    }

    return(super.onOptionsItemSelected(item));
  }

  boolean isSingleShotProcessing() {
    return(singleShotProcessing);
  }

  Contract getContract() {
    return((Contract)getActivity());
  }

  interface Contract {
    boolean isSingleShotMode();

    void setSingleShotMode(boolean mode);
  }

  class DemoCameraHost extends SimpleCameraHost {
    public DemoCameraHost(Context _ctxt) {
      super(_ctxt);
    }

    @Override
    public boolean useFrontFacingCamera() {
      return(getArguments().getBoolean(KEY_USE_FFC));
    }

    @Override
    public boolean useSingleShotMode() {
      return(singleShotItem.isChecked());
    }

    @Override
    public void saveImage(byte[] image) {
      if (useSingleShotMode()) {
        singleShotProcessing=false;

        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            takePictureItem.setEnabled(true);
          }
        });

        DisplayActivity.imageToShow=image;
        startActivity(new Intent(getActivity(), DisplayActivity.class));
      }
      else {
        super.saveImage(image);
      }
    }

    @Override
    public void autoFocusAvailable() {
      autoFocusItem.setEnabled(true);
    }

    @Override
    public void autoFocusUnavailable() {
      autoFocusItem.setEnabled(false);
    }

    @Override
    public void onCameraFail(CameraHost.FailureReason reason) {
      super.onCameraFail(reason);

      Toast.makeText(getActivity(),
                     "Sorry, but you cannot use the camera now!",
                     Toast.LENGTH_LONG).show();
    }
  }
}