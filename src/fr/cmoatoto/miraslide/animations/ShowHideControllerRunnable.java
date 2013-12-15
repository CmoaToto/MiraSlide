package fr.cmoatoto.miraslide.animations;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout.LayoutParams;

public class ShowHideControllerRunnable implements Runnable {

	private View controllerView;
	private View backStackView;

	private ValueAnimator controllerAnimation;

	// At first, the controller is hidden.
	private boolean show = false;
	private int controllerWidth = -1;
	private int currentMargin = -1;

	// listeners
	private List<OnShowChangeListener> listeners = new ArrayList<OnShowChangeListener>();

	/**
	 * Create the runnable used to show or hide the controller Fragment.
	 * 
	 * @param controllerView
	 *            the view of the controller Fragment
	 * @param backStackView
	 *            the view behind the controller Fragment. It's needed to set it to GONE when the controller is completely hiding this view.
	 */
	public ShowHideControllerRunnable(View controllerView, View backStackView) {
		this.controllerView = controllerView;
		this.backStackView = backStackView;
	}

	/**
	 * Use this method to set initial values before launching the runnable. It is useful if the view is behind the two states (VISIBLE/HIDDEN).
	 * 
	 * @param show
	 *            true if we want to show the controller, false otherwise
	 * @param currentMargin
	 *            the start value of the margin (in px)
	 */
	public void setInitialValues(boolean show, int currentMargin) {
		this.show = !show;
		this.currentMargin = currentMargin;
	}

	@Override
	public void run() {
		// We need the width of the view. if it is (IN)VISIBLE, we keep the value in controllerWidth
		// If it is GONE, we take the last value used.
		// If their is no last value, return.
		if (controllerView.getWidth() > 0) {
			controllerWidth = controllerView.getWidth();
		}
		if (controllerWidth < 0) {
			return;
		}

		// Invert the state of the view
		show = !show;
		for (OnShowChangeListener listener : listeners) {
			listener.onShowChange(show);
		}

		// Cancel any running animation
		if (controllerAnimation != null && controllerAnimation.isRunning()) {
			controllerAnimation.cancel();
		}
		// Define the values for the animation
		if (currentMargin != -1) {
			controllerAnimation = ValueAnimator.ofInt(currentMargin, show ? 0 : -controllerWidth);
		} else {
			controllerAnimation = ValueAnimator.ofInt(show ? -controllerWidth : 0, show ? 0 : -controllerWidth);
		}

		// Define the parameters of the animation
		controllerAnimation.setDuration(500);
		controllerAnimation.setInterpolator(new DecelerateInterpolator());
		controllerAnimation.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// During the animation, we change the left/right margins of the view to create a slide
				LayoutParams params = (LayoutParams) controllerView.getLayoutParams();
				currentMargin = (Integer) animator.getAnimatedValue();
				params.rightMargin = currentMargin;
				params.leftMargin = -currentMargin;
				controllerView.setLayoutParams(params);
			}
		});
		controllerAnimation.addListener(new AnimatorListener() {

			private boolean hasBeenCanceled = false;

			@Override
			public void onAnimationStart(Animator animation) {
				// The front(controller)/back views need to be VISIBLE during the animation
				if (controllerView.getVisibility() != View.VISIBLE) {
					controllerView.setVisibility(View.VISIBLE);
				}
				if (backStackView.getVisibility() != View.VISIBLE) {
					backStackView.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!hasBeenCanceled) {
					// The animation has been ended naturally so one of the two view is not shown anymore
					// so we GONE it, and reset the currentMargin value.
					if (!show) {
						controllerView.setVisibility(View.GONE);
					} else {
						backStackView.setVisibility(View.GONE);
					}
					currentMargin = -1;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// The animation has been canceled, because of a new animation ready to go, or because
				// of a swype. We need to keep the current values.
				hasBeenCanceled = true;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		controllerAnimation.start();
	}

	/** Cancel any current sliding animation */
	public void cancel() {
		if (controllerAnimation != null && controllerAnimation.isRunning()) {
			controllerAnimation.cancel();
		}
	}

	public boolean isShown() {
		return show;
	}

	public interface OnShowChangeListener {
		public void onShowChange(boolean show);
	}

	public void addOnShowChangeListener(OnShowChangeListener listener) {
		listeners.add(listener);
	}

	public void removeOnShowChangeListener(OnShowChangeListener listener) {
		listeners.remove(listener);
	}
}
