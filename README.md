BlurZoomGallery
================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fafaldo/blur-zoom-gallery/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.fafaldo/blur-zoom-gallery)

Extended CoordinatorLayout, that helps creating background galleries.

![Illustration of behavior](https://github.com/fafaldo/BlurZoomGallery/blob/master/blurzoomgallery.gif "Illustration of behavior")


Features:
--------------

- expandable Collapsing Toolbar Layout, making space for a background view
- hadling of expand/collapse animation
- blurring of background view on collapse animation
- zoom effect on collapse


How to use
----------

Import dependency using Gradle:

```
compile 'com.github.fafaldo:blur-zoom-gallery:1.0.0'
```


In order to use BlurZoomGallery you need to implement following view hierarchy in your XML layout file:

```		
|
|-> BlurZoomCoordinatorLayout
	|
	|-> Gallery container layout (id: gallery_coordinator_gallery_container)
	|	|
	|	|-> Gallery view (for example ViewPager; id: gallery_coordinator_gallery)
	|
	|-> AppBarLayout (id: gallery_coordinator_appbarlayout)
	|	|
	|	|-> CollapsingToolbarLayout
	|		|
	|		|-> Toolbar (id: gallery_coordinator_toolbar)
	|
	|-> Scrollable view (for example NestedScrollView; id: gallery_coordinator_scroll)
		|
		|-> Placeholder view that will be expanded, must be first element of view to make it work (id: gallery_coordinator_placeholder)
		...
```

Remember to assign each element a proper ID. This structure is very similar to structure implemented in Google's Support Design Library, for obvious reasons.
Background gallery view should be placed inside a container, the container will be drawn and blurred but only the inner view will receive touch events.
Placeholder view should be invisible and is used to expand upper and lower views. 

Example implementation:
 
```xml
<com.github.fafaldo.blurzoomgallery.widget.BlurZoomCoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:id="@+id/coordinator">

    <FrameLayout
        android:id="@+id/gallery_coordinator_gallery_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <android.support.v4.view.ViewPager
            android:id="@+id/gallery_coordinator_gallery"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>

    </FrameLayout>

    <android.support.design.widget.AppBarLayout
        android:id="@+id/gallery_coordinator_appbarlayout"
        android:layout_height="300dp"
        android:layout_width="match_parent"
        android:background="@android:color/transparent"
        app:elevation="0dp">

        <android.support.design.widget.CollapsingToolbarLayout
            android:id="@+id/collapsing_toolbar"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            app:expandedTitleTextAppearance="@style/MyExpandedTextAppearance"
            app:collapsedTitleTextAppearance="@style/MyCollapsedTextAppearance"
            app:expandedTitleMargin="50dp"
            android:background="@android:color/transparent">

            <android.support.v7.widget.Toolbar
                android:id="@+id/gallery_coordinator_toolbar"
                android:layout_height="?attr/actionBarSize"
                android:layout_width="match_parent"
                app:layout_collapseMode="pin"/>

        </android.support.design.widget.CollapsingToolbarLayout>

    </android.support.design.widget.AppBarLayout>

    <android.support.v4.widget.NestedScrollView
        android:id="@+id/gallery_coordinator_scroll"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior"
        android:paddingLeft="20dp"
        android:paddingRight="20dp">

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <View
                android:id="@+id/gallery_coordinator_placeholder"
                android:layout_width="match_parent"
                android:layout_height="0dp"/>

            ...

        </RelativeLayout>

    </android.support.v4.widget.NestedScrollView>

</com.github.fafaldo.blurzoomgallery.widget.BlurZoomCoordinatorLayout>
```

Gallery will be automatically expanded on Collapsing Toolbar click. If you want to do it manually use function
```
expand();
```

Gallery collapses automatically on scrollable view click. To do it manually call
```
collapse();
```

If you want to listen for collapse and expand changes set the OnStateChangedListener.
Blurring action takes some time so it's done asynchronously. To listen for success of blurring set OnBlurCompletedListener.

In case of other problems with implementation see example included in this repository.


Animation duration and interpolator
-----

You can control duration and interpolator of the animator used to expand and collapse Toolbar with methods:
```
setInterpolator(Interpolator interpolator);
setDuration(int duration);
```

However, Google APIs doesn't allow this kind of customization yet. My implementation uses Java's reflection mechanism, so be aware that this code might stop working in the future.

Blur animation is done using frame by frame animation, with each frame blurred more and more. ImageViews are added dynamically from the code. You can control max blur radius and number of blur steps. Remember, that adding too many ImageView might affect performance. In order to reduce memory consumption images are downscaled before blurring. You can control this downscaling with one of the parameters.

To properly use the selected duration and interpolator you should play expend/collapse animation on AppBarLayout at least once, before trying to open gallery by touching. You should call
```
appBarLayout.setExpanded(true, true);
```
By default AppBarLayout is expanded, so call function with first parameter set to 'true', to avoid unwanted animation on start-up. Remember to set second parameter to 'true' too, because only then will the inner fields be properly initiated.


Parameters:
-----

You can control these parameters via XML:

```
<attr name="collapsedListHeight" format="dimension"/>	//height of the scrollable view that will be left after view expand (in dp), default: 112 px
<attr name="maxBlurRadius" format="float"/>				//maximum radius of blur, default: 4
<attr name="blurSteps" format="integer"/>				//steps of blur animation, default: 5
<attr name="bitmapSizeDivide" format="integer"/>		//how many times are images resized, before blurring, default: 5
<attr name="blurEnable" format="boolean"/>				//enable blur, default: true
<attr name="scaleEnable" format="boolean"/>				//enable scale animation, default: true
<attr name="maxScale" format="float"/>					//maximum zoom of collapsed anim, default: 1.15
<attr name="android:scaleType"/>						//scaleType of blur steps ImageViews, default: CENTER_CROP
```


Changelog
---------

* 1.0.0 - initial release


License
----

FABToolbar for Android

The MIT License (MIT)

Copyright (c) 2015 Rafał Tułaza

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.