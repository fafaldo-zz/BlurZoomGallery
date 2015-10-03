package com.github.fafaldo.blurzoomgallery.sample;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.fafaldo.blurzoomgallery.widget.BlurZoomCoordinatorLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;


public class ImageFragment extends Fragment {
    private static final String PHOTO = "photo";
    private static final String PAGE = "page";

    private String photo = "";
    private int page;

    private ImageView imageView;

    private boolean isLoading = false;

    public static ImageFragment newInstance(String photo, int page) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(PHOTO, photo);
        args.putInt(PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    public ImageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photo = getArguments().getString(PHOTO);
            page = getArguments().getInt(PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imageView = (ImageView) inflater.inflate(R.layout.fragment_image, container, false);

        ImageLoader.getInstance().loadImage(
                photo,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        isLoading = true;
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.image_loading_bg));
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        isLoading = false;
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                        isLoading = false;
                        if (getActivity() != null) {
                            if (((MainActivity) getActivity()).isScrolling == true) {
                                imageView.setImageBitmap(loadedImage);
                                Log.d("ToolbarZoomGalleryEvent", page + ": we're scrolling, just display");
                            } else {
                                if (((MainActivity) getActivity()).currentPage == page) {
                                    if (((MainActivity) getActivity()).isExpanded) {
                                        ((MainActivity) getActivity()).blur(loadedImage, new BlurZoomCoordinatorLayout.OnBlurCompletedListener() {
                                            @Override
                                            public void blurCompleted() {
                                                imageView.setImageBitmap(loadedImage);
                                            }
                                        });

                                        Log.d("ToolbarZoomGalleryEvent", page + ": we're not scrolling, loaded current photo, is expanded, blur and display");
                                    } else {
                                        ((MainActivity) getActivity()).blur(loadedImage, new BlurZoomCoordinatorLayout.OnBlurCompletedListener() {
                                            @Override
                                            public void blurCompleted() {
                                                imageView.setImageBitmap(loadedImage);
                                            }
                                        });

                                        Log.d("ToolbarZoomGalleryEvent", page + ": we're not scrolling, loaded current photo, is collapsed, blur and display");
                                    }
                                } else {
                                    imageView.setImageBitmap(loadedImage);

                                    Log.d("ToolbarZoomGalleryEvent", page + ": we're not scrolling, loaded other photo (current " + ((MainActivity) getActivity()).currentPage + ") so just display");
                                }
                            }
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        isLoading = false;
                    }
                }
        );

        return imageView;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
