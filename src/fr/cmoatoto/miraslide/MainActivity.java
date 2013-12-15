package fr.cmoatoto.miraslide;

import java.lang.reflect.Field;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import fr.cmoatoto.miraslide.animations.ShowHideControllerRunnable;
import fr.cmoatoto.miraslide.fragments.ControllerFragment;

/**
 * Main Activity. It is launch as MAIN LAUNCHER. The Activity handle 2 fragments. The first one is for selecting the PDF, the display and launch the
 * presentation. The second is for controlling the presentation, and show a chronometer, current slide... <br>
 * <br>
 * This Activity handle the presentation so it is really important that we don't switch to another activity. Otherwise the presentation will be set to pause
 * (and the Wireless Display will replicate your device screen) until we get back to the Activity.
 * 
 * @author CmoaToto
 */
public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getName();

	// Selection fragment is the fragment to select the PDF & the display, then launch the presentation
	private View mSelectionFragmentView;
	private Fragment mSelectionFragment;

	// Controller fragment is the fragment to control the display, which slide to show etc...
	private View mControllerFragmentView;
	private ControllerFragment mControllerFragment;

	/** Menu element to show or hide the controller Fragment */
	private MenuItem mShowHideControllerActionBarButton;

	/** The presentation is a special kind of dialog whose purpose is to present content on a secondary display. */
	private PdfViewerPresentation mPresentation;

	// Handler & Runnable to control the controller view show/hide animation
	private Handler mShowHideControllerHandler = new Handler();
	private ShowHideControllerRunnable mShowHideControllerRunnable;

	/** The display where we show the presentation */
	private Display mDisplay;

	/** The path to the pdf file to show. In fact it is always the same path, as we copy the file in an internal folder */
	private String mPdfPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mSelectionFragmentView = findViewById(R.id.activity_main_fragment_selection);
		mControllerFragmentView = findViewById(R.id.activity_main_fragment_controller);

		mSelectionFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_fragment_selection);
		mControllerFragment = (ControllerFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main_fragment_controller);

		// Set the controller view to INVISIBLE while creating to calculate its width for sliding.
		// Set to VISIBLE/GONE after the first slide.
		mShowHideControllerRunnable = new ShowHideControllerRunnable(mControllerFragmentView, mSelectionFragmentView);
		mControllerFragmentView.setVisibility(View.INVISIBLE);

		// Trick to always show the menu button in the ActionBar
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
			// Ignore
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// creation of the show/hide controller fragment menu button
		mShowHideControllerActionBarButton = menu.add(Menu.NONE, 1, Menu.NONE, "Control slides");
		mShowHideControllerActionBarButton.setIcon(R.drawable.ic_action_keyboard);
		mShowHideControllerActionBarButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// enable button only when slides are loaded and display selected
		mShowHideControllerActionBarButton.setEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Enable or disable the 'Show/Hide Controller fragment' ActionBar Button. The button should be enabled only if slides are loaded and display selected.
	 * 
	 * @param enable
	 *            : true if the button should be enable. false otherwise.
	 */
	public void enableShowHideControllerActionBarButton(boolean enable) {
		mShowHideControllerActionBarButton.setEnabled(enable);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			// If ShowHideController button is selected
			showHideController();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/** If the Controller fragment is shown, a back press hide it. */
	@Override
	public void onBackPressed() {
		if (mShowHideControllerRunnable.isShown()) {
			showHideController(false);
		} else {
			super.onBackPressed();
		}
	}

	/** Show or hide controller fragment */
	public void showHideController() {
		mShowHideControllerHandler.removeCallbacks(mShowHideControllerRunnable);
		mShowHideControllerHandler.post(mShowHideControllerRunnable);
	}

	/**
	 * Show or hide controller fragment. If the controller fragment is already in the right position, this method do nothing.
	 * 
	 * @param show
	 *            : If the controller should be shown after the operation.
	 */
	public void showHideController(boolean show) {
		if (mShowHideControllerRunnable.isShown() != show) {
			showHideController();
		}
	}

	/**
	 * Set the presentation (created in the selection fragment). This method creates listeners to control the visibility of the controller fragment and if the
	 * mShowHideControllerActionBarButton should be enabled. Then it shows the presentation.
	 * 
	 * @param presentation
	 *            : the presentation to show
	 */
	public void launchPresentation() {
		mPresentation = new PdfViewerPresentation(this, mDisplay, mPdfPath);

		mPresentation.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				showHideController(true);
				enableShowHideControllerActionBarButton(true);
				mControllerFragment.notifyViewPager();
			}
		});
		mPresentation.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				showHideController(false);
				enableShowHideControllerActionBarButton(false);
			}
		});

		mPresentation.show();
	}

	/** return the Presentation if it has been created. A presentation needs a display and a pdf file */
	public PdfViewerPresentation getPresentation() {
		return mPresentation;
	}

	/** Stop the presentation and hide the controller fragment */
	public void stopPresentation() {
		showHideController(false);
		mPresentation = null;
		mControllerFragment.notifyViewPager();
	}

	/** return the display if there is one selected */
	public Display getDislay() {
		return mDisplay;
	}

	/** Set the display to show the presentation */
	public void setDisplay(Display display) {
		this.mDisplay = display;
	}

	/** return the path to the pdf file */
	public String getPdfPath() {
		return mPdfPath;
	}

	/** set the path to the pdf file */
	public void setPdfPath(String pdfPath) {
		this.mPdfPath = pdfPath;
	}

}
