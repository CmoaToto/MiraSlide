package fr.cmoatoto.miraslide.fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import fr.cmoatoto.miraslide.MainActivity;
import fr.cmoatoto.miraslide.R;

/**
 * This fragment presents the slide on the external display (controllable with a viewpager), the page number, 
 * two buttons prev and next, and a chronometer, very useful for a speaker.
 * 
 * @author CmoaToto
 *
 */
public class ControllerFragment extends Fragment implements OnClickListener {

	// The viewpager and the adapter to show the slides
	private SlidesPagerAdapter mSlidesPagerAdapter;
	private ViewPager mSlidesViewPager;
	
	// A boolean to know if the chronometer is running, and one to store the stopped
	// value if the chronometer has been stopped.
	private boolean mIsChronometerRunning = false;
	private long mChronometerStoppedTime = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_controller, container, false);

		v.findViewById(R.id.fragment_controller_chronometer).setOnClickListener(this);
		v.findViewById(R.id.fragment_controller_chronometer_button_reset).setOnClickListener(this);
		v.findViewById(R.id.fragment_controller_button_pagenext).setOnClickListener(this);
		v.findViewById(R.id.fragment_controller_button_pageprev).setOnClickListener(this);
		
		createViewPager(v);

		return v;
	}
	
	/** We create the viewpager which shows the pages of the pdf file. If the user change the slide, the listener
	 * updates tell the parent Activity to change the image in the presentation.
	 * 
	 * @param v : the fragment view
	 */
	private void createViewPager(View v) {
		mSlidesPagerAdapter = new SlidesPagerAdapter(((FragmentActivity) getActivity()).getSupportFragmentManager());
		mSlidesViewPager = (ViewPager) v.findViewById(R.id.fragment_controller_pager);
		mSlidesViewPager.setAdapter(mSlidesPagerAdapter);
		mSlidesViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			
			@Override
			public void onPageSelected(int page) {
				((MainActivity) getActivity()).getPresentation().moveTo(page);
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_controller_chronometer) {
			// On click on the chronometer, we start or stop the chrono and update the image behind
			if (mIsChronometerRunning) {
				((Chronometer) v).stop();
				mChronometerStoppedTime = SystemClock.elapsedRealtime() - ((Chronometer) v).getBase();
				((ImageView) getView().findViewById(R.id.fragment_controller_chronometer_imageview_playpause)).setImageResource(R.drawable.ic_action_play);
				mIsChronometerRunning = false;
			} else {
				((Chronometer) v).setBase(SystemClock.elapsedRealtime() - mChronometerStoppedTime);
				((Chronometer) v).start();
				((ImageView) getView().findViewById(R.id.fragment_controller_chronometer_imageview_playpause)).setImageResource(R.drawable.ic_action_pause);
				mIsChronometerRunning = true;
			}

		} else if (v.getId() == R.id.fragment_controller_chronometer_button_reset) {
			// On click on the reset button, we stop the chronometer and set it back to 0
			((Chronometer) getView().findViewById(R.id.fragment_controller_chronometer)).setBase(SystemClock.elapsedRealtime());
			((Chronometer) getView().findViewById(R.id.fragment_controller_chronometer)).stop();
			((ImageView) getView().findViewById(R.id.fragment_controller_chronometer_imageview_playpause)).setImageResource(R.drawable.ic_action_play);
			mChronometerStoppedTime = 0;
			mIsChronometerRunning = false;

		} else if (v.getId() == R.id.fragment_controller_button_pageprev) {
			// On click on the prev button, we move the viewpager one slide back (which will move the presentation slide as well)
			mSlidesViewPager.setCurrentItem(mSlidesViewPager.getCurrentItem() - 1);

		} else if (v.getId() == R.id.fragment_controller_button_pagenext) {
			// On click on the next button, we move the viewpager one slide next (which will move the presentation slide as well)
			mSlidesViewPager.setCurrentItem(mSlidesViewPager.getCurrentItem() + 1);
		}
	}

	/** The adapter of the slides viewpager. It shows SlideFragments */
	private class SlidesPagerAdapter extends FragmentStatePagerAdapter {

		public SlidesPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new SlideFragment();
			Bundle args = new Bundle();
			args.putInt(SlideFragment.KEY_PAGE, i);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return ((MainActivity) getActivity()).getPresentation() == null ? 0 : ((MainActivity) getActivity()).getPresentation().getPageCount();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Page " + (position + 1);
		}

	}

	/** When there is a change in the pdf, we notify the viewpager to recreate himself. */
	public void notifyViewPager() {
		createViewPager(getView());
	}

}
