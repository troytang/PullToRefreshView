# PullToRefreshView #
`PullToRefreshView` is base on `ListView`. Other `PullToRefreshLayout`s will prevent the touch event to `ListView` when the refresh occur. So I make it on the `ListView`.

# Usage #
### Gradle ###
1.Add this dependency to your build.gradle file:
```
dependencies {
    compile 'com.tangwy:pulltorefreshview:0.1.0'
}
```

### Basic Usage ###

* Add it in the xml
```
<com.tangwy.pulltorefreshview.PullToRefreshListView
  android:id="@+id/lv"
  android:layout_width="match_parent"
  android:layout_height="match_parent"/>
```

* Implement and set the OnRefreshListenter
```
public class MainActivity implements PullToRefreshListView.OnRefreshListener {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    ...
    PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.lv);
    listView.setOnRefreshListener(this);
    ...
  }
  
  @Override
  public void onRefresh() {
    // do something
  }
  ...
}
```
* Controll the refresh behavior
```
// start refresh, call this will be call onRefresh() automatic
listView.startRefresh();
// refresh completed
listView.refreshCompleted();
```

# License #

```
Copyright 2015 Troy Tang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

