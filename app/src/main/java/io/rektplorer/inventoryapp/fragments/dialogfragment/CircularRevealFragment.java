package io.rektplorer.inventoryapp.fragments.dialogfragment;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.Arrays;
import java.util.Collections;

import io.rektplorer.inventoryapp.ItemProfileContainerActivity;
import io.rektplorer.inventoryapp.R;

public class CircularRevealFragment extends Fragment{

    private OnFragmentTouched listener;
    private int fragmentLayoutRes;

    public CircularRevealFragment(){
    }

    public static CircularRevealFragment newInstance(int fragmentLayoutRes, int centerX, int centerY){
        Bundle args = new Bundle();
        args.putInt("resLayout", fragmentLayoutRes);
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        // args.putInt("color", color);

        CircularRevealFragment fragment = new CircularRevealFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(getArguments() != null){
            fragmentLayoutRes = getArguments().getInt("resLayout");
        }
        View rootView = inflater.inflate(fragmentLayoutRes, container, false);
        // rootView.setBackgroundColor(getArguments().getInt("color"));

        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();

        final Integer[] rightA = new Integer[2];

        final int cx = getArguments().getInt("cx");
        final int cy = getArguments().getInt("cy");

        // To run the animation as soon as the view is layout in the view hierarchy we add this
        // listener and remove it
        // as soon as it runs to prevent multiple animations if the view changes bounds
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom){
                v.removeOnLayoutChangeListener(this);

                rightA[0] = left;
                rightA[1] = bottom;

                // get the hypotenuse so the radius is from one corner to the other
                int radius = (int) Math.hypot(right, bottom);

                Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                reveal.setInterpolator(new DecelerateInterpolator(2f));
                reveal.setDuration(2000);
                reveal.start();
            }
        });

        // Set OnBackPressed Animation
        rootView.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(getActivity() instanceof ItemProfileContainerActivity && event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    int radius = (int) Math.hypot(rightA[0], rightA[1]);

                    Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, radius, 0);
                    reveal.setInterpolator(new DecelerateInterpolator(2f));
                    reveal.addListener(new Animator.AnimatorListener(){
                        @Override
                        public void onAnimationStart(Animator animation){

                        }

                        @Override
                        public void onAnimationEnd(Animator animation){
                            getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                            // getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                            getActivity().finish();
                            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation){

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation){

                        }
                    });
                    reveal.setDuration(320);
                    reveal.start();
                }
                return false;
            }
        });

        // attach a touch listener
        rootView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(listener != null){
                    listener.onFragmentTouched(CircularRevealFragment.this, event.getX(), event.getY());
                }
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof OnFragmentTouched){
            listener = (OnFragmentTouched) activity;
        }
    }

    /**
     * Get the animator to unreveal the circle
     *
     * @param cx center x of the circle (or where the view was touched)
     * @param cy center y of the circle (or where the view was touched)
     *
     * @return Animator object that will be used for the animation
     */
    public Animator prepareUnrevealAnimator(float cx, float cy){
        int radius = getEnclosingCircleRadius(getView(), (int) cx, (int) cy);
        Animator anim = ViewAnimationUtils.createCircularReveal(getView(), (int) cx, (int) cy, radius, 0);
        anim.setInterpolator(new AccelerateInterpolator(2f));
        anim.setDuration(1000);
        return anim;
    }

    /**
     * To be really accurate we have to start the circle on the furthest corner of the view
     *
     * @param v  the view to unreveal
     * @param cx center x of the circle
     * @param cy center y of the circle
     *
     * @return the maximum radius
     */
    private int getEnclosingCircleRadius(View v, int cx, int cy){
        int realCenterX = cx + v.getLeft();
        int realCenterY = cy + v.getTop();
        int distanceTopLeft = (int) Math.hypot(realCenterX - v.getLeft(), realCenterY - v.getTop());
        int distanceTopRight = (int) Math.hypot(v.getRight() - realCenterX, realCenterY - v.getTop());
        int distanceBottomLeft = (int) Math.hypot(realCenterX - v.getLeft(), v.getBottom() - realCenterY);
        int distanceBottomRight = (int) Math.hypot(v.getRight() - realCenterX, v.getBottom() - realCenterY);

        Integer[] distances = new Integer[]{distanceTopLeft, distanceTopRight, distanceBottomLeft,
                                            distanceBottomRight};

        return Collections.max(Arrays.asList(distances));
    }

    public void setOnFragmentTouchedListener(OnFragmentTouched onFragmentTouchedListener){
        this.listener = onFragmentTouchedListener;
    }

    public interface OnFragmentTouched{
        void onFragmentTouched(Fragment fragment, float x, float y);
    }


}