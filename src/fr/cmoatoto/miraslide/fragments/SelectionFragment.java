package fr.cmoatoto.miraslide.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import fr.cmoatoto.miraslide.MainActivity;
import fr.cmoatoto.miraslide.R;

/**
 * This fragment is the first fragment the user see. Its goal is to select the pdf and the display to show, then launch the presentation. <br>
 * <br>
 * The 3 buttons react to the state of if the elements (pdf and display) are selected or not, and if the projection can be launch.
 * 
 * @author CmoaToto
 */
public class SelectionFragment extends Fragment implements OnClickListener, DisplayListener {

	private static final String TAG = SelectionFragment.class.getName();

	/** Code for the ActivityForResult for the PDF file selection */
	public static int SELECT_PDF_FILE_INTENT_RESULT_CODE = 712;

	/** Code for the ActivityForResult for the Speaker's notes file selection */
	public static int SELECT_NOTES_FILE_INTENT_RESULT_CODE = 713;

	/** The display manager is the object to get informations about the different displays */
	private DisplayManager mDisplayManager;

	/** The parent Activity */
	private MainActivity mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (MainActivity) getActivity();

		// We get the display manager to get info about the display. We also register to any change about the (dis)connection
		// of the displays.
		mDisplayManager = (DisplayManager) mActivity.getSystemService(Context.DISPLAY_SERVICE);
		mDisplayManager.registerDisplayListener(this, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_selection, container, false);

		v.findViewById(R.id.fragment_selection_button_choosepdf).setOnClickListener(this);
		v.findViewById(R.id.fragment_selection_button_choosenotes).setOnClickListener(this);
		v.findViewById(R.id.fragment_selection_button_choosenotes_info).setOnClickListener(this);
		v.findViewById(R.id.fragment_selection_button_selectwirelessdisplay).setOnClickListener(this);
		v.findViewById(R.id.fragment_selection_button_launchprojection).setOnClickListener(this);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		// At resuming, we check the state of each buttons.
		checkLaunchable(getView());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_selection_button_choosepdf) {
			// On click on the "select pdf" button, we launch a file explorer to let the user select the pdf file.
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("application/pdf");
			startActivityForResult(Intent.createChooser(intent, "Select PDF"), SELECT_PDF_FILE_INTENT_RESULT_CODE);

		} else if (v.getId() == R.id.fragment_selection_button_choosenotes) {
			// On click on the "select notes" button, we launch a file explorer to let the user select the speaker's notes file.
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("text/*");
			startActivityForResult(Intent.createChooser(intent, "Select Speaker's notes"), SELECT_NOTES_FILE_INTENT_RESULT_CODE);

		} else if (v.getId() == R.id.fragment_selection_button_choosenotes_info) {
			// On click on the "info button" of the "select notes" button, we show a dialog to explain how to load notes.
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity).setIcon(R.drawable.ic_launcher).setTitle("How to select Speaker's notes")
					.setNeutralButton("Ok", null).setMessage(getText(R.string.choose_notes_file_info));
			builder.show();

		} else if (v.getId() == R.id.fragment_selection_button_selectwirelessdisplay) {
			// On click on the "select display" button, we check if there is external display connected.
			final Display[] displays = mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
			if (displays.length == 0) {
				// If there is no external display connected, we launch the Display settings. We could launch the Wifi display
				// settings with ACTION_WIFI_DISPLAY_SETTINGS but it is an hidden static value because there may be not such settings
				// (if the device doesn't have Wireless display but have API >= 17).
				startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
			} else {
				// If there is one or more external display, we show a dialog box with the list of the display. The user can select
				// the display he wants, or close the dialog.
				final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.select_dialog_singlechoice);
				for (int i = 0; i < displays.length; i++) {
					arrayAdapter.add(displays[i].getName());
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity).setIcon(R.drawable.ic_launcher).setTitle("Select a display")
						.setNegativeButton("cancel", null).setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// When the user choose a display through the dialog, we set it in the parent Activity and update
								// the state of the buttons.
								mActivity.setDisplay(displays[which]);
								checkLaunchable(getView());
							}
						});
				builder.show();
			}

		} else if (v.getId() == R.id.fragment_selection_button_launchprojection) {
			// On click on the "launch projection" button, we... launch the projection
			mActivity.launchPresentation();

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SELECT_PDF_FILE_INTENT_RESULT_CODE) {
				// When the user has selected a pdf file through a file explorer, we try to copy the file
				// in the application data folder and check if the file can be read.
				// If everything is ok, we store the path of the file (the one in the app data folder) and
				// update the buttons state.
				Uri dataUri = data.getData();

				if (data != null && dataUri.getPath() != null) {
					String pdfName = getFilename(dataUri);
					File pdfFile = storeUriContentToFile(dataUri);
					if (!pdfFile.canRead()) {
						Toast.makeText(mActivity, "Can't read file", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mActivity, "File loaded : " + pdfName, Toast.LENGTH_SHORT).show();
						((TextView) getView().findViewById(R.id.fragment_selection_button_choosepdf)).setText("File " + pdfName + " loaded");
						mActivity.setPdfPath(pdfFile.getAbsolutePath());
						checkLaunchable(getView());
					}
				} else {
					Toast.makeText(mActivity, "Invalid file data", Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == SELECT_NOTES_FILE_INTENT_RESULT_CODE) {
				// When the user has selected a speaker's note file through a file explorer, we try to parse the file
				// to create notes.
				// If everything is ok, we update the buttons state.
				Uri dataUri = data.getData();

				if (data != null && dataUri.getPath() != null) {
					String notesName = getFilename(dataUri);
					SparseArray<String> notes = parseNotesFile(dataUri);
					if (notes == null || notes.size() == 0) {
						Toast.makeText(mActivity, "Can't read notes", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mActivity, "Speaker's Notes loaded : " + notesName, Toast.LENGTH_SHORT).show();
						((TextView) getView().findViewById(R.id.fragment_selection_button_choosenotes)).setText("Speaker's Notes  " + notesName + " loaded");
						mActivity.setNotes(notes);
						checkLaunchable(getView());
					}
				} else {
					Toast.makeText(mActivity, "Invalid file data", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * We check the state of the external displays, update the text of the display button, and if there is only one extenral display, we auto select it
	 */
	private void checkDisplay(TextView displayButton) {
		Display[] displays = mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

		if (displays.length > 1 && mActivity.getDislay() == null) {
			displayButton.setText("Select a wireless display");
		} else if (displays.length == 1) {
			mActivity.setDisplay(displays[0]);
			displayButton.setText("Display selected " + displays[0].getName());
		} else {
			mActivity.setDisplay(null);
			displayButton.setText("Connect to a wireless display");
		}
	}

	/**
	 * We check the selection of the pdf file and the display, and we update the color of the buttons and if the "launch projection" button should be enable
	 * 
	 * @param v
	 *            : the global view of the fragment
	 */
	private void checkLaunchable(View v) {
		checkDisplay((TextView) v.findViewById(R.id.fragment_selection_button_selectwirelessdisplay));

		if (mActivity.getPdfPath() != null) {
			v.findViewById(R.id.fragment_selection_button_choosepdf).setBackgroundResource(R.drawable.button_green);
		} else {
			v.findViewById(R.id.fragment_selection_button_choosepdf).setBackgroundResource(R.drawable.button_red);
		}

		if (mActivity.getNotes() != null && mActivity.getNotes().size() > 0) {
			v.findViewById(R.id.fragment_selection_button_choosenotes).setBackgroundResource(R.drawable.button_green);
		} else {
			v.findViewById(R.id.fragment_selection_button_choosenotes).setBackgroundResource(R.drawable.button_orange);
		}

		if (mActivity.getDislay() != null) {
			v.findViewById(R.id.fragment_selection_button_selectwirelessdisplay).setBackgroundResource(R.drawable.button_green);
		} else {
			v.findViewById(R.id.fragment_selection_button_selectwirelessdisplay).setBackgroundResource(R.drawable.button_red);
		}

		if (mActivity.getDislay() != null && mActivity.getPdfPath() != null) {
			v.findViewById(R.id.fragment_selection_button_launchprojection).setEnabled(true);
			v.findViewById(R.id.fragment_selection_button_launchprojection).setBackgroundResource(R.drawable.button_green);
		} else {
			v.findViewById(R.id.fragment_selection_button_launchprojection).setEnabled(false);
			v.findViewById(R.id.fragment_selection_button_launchprojection).setBackgroundResource(R.drawable.button_red);
		}

		// We try to set the focus to the most important button
		if (mActivity.getPdfPath() == null) {
			v.findViewById(R.id.fragment_selection_button_choosepdf).requestFocusFromTouch();
		} else if (mActivity.getNotes() == null || mActivity.getNotes().size() == 0) {
			v.findViewById(R.id.fragment_selection_button_choosenotes).requestFocusFromTouch();
		} else if (mActivity.getDislay() == null) {
			v.findViewById(R.id.fragment_selection_button_selectwirelessdisplay).requestFocusFromTouch();
		} else {
			v.findViewById(R.id.fragment_selection_button_launchprojection).requestFocusFromTouch();
		}
	}

	/**
	 * Return the filename from a uri.
	 */
	private String getFilename(Uri uri) {
		try {
			String scheme = uri.getScheme();
			if (scheme.equals("file")) {
				return uri.getLastPathSegment();
			} else if (scheme.equals("content")) {
				String[] proj = { MediaStore.Files.FileColumns.DISPLAY_NAME };
				Cursor cursor = mActivity.getContentResolver().query(uri, proj, null, null, null);
				if (cursor != null && cursor.getCount() != 0) {
					int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
					cursor.moveToFirst();
					return cursor.getString(columnIndex);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Copy the content of the file pointed by the uri to a file in the application data folder.
	 * 
	 * @param uri
	 *            : The uri of the file to store
	 * 
	 * @return the File created, the copy from the file pointed by the uri.
	 */
	private File storeUriContentToFile(Uri uri) {
		File file = null;
		try {
			File root = mActivity.getFilesDir();
			if (root == null)
				throw new Exception("data dir not found");
			file = new File(root, "MiraSlide_tmp.pdf");
			file.delete();
			InputStream is = mActivity.getContentResolver().openInputStream(uri);
			OutputStream os = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int cnt = is.read(buf);
			while (cnt > 0) {
				os.write(buf, 0, cnt);
				cnt = is.read(buf);
			}
			os.close();
			is.close();
			file.deleteOnExit();
		} catch (Exception e) {
			Log.e("OpenFile", e.getMessage(), e);
		}
		return file;
	}

	/**
	 * Parse the content of the file pointed by the Uri.<br>
	 * <br>
	 * The file <b>can be a plain text file</b>. Then each note should be delimited by the tags <code>&lt;page=#&gt;</code> (where # is the n° of the
	 * slide, starting at 1) and <code>&lt;/page&gt;</code><br>
	 * <br>
	 * It <b>can also be an html file from Google Drive Presentation</b>. To get it :
	 * <ul>
	 * <li>Open your Google Drive Presentation (from a desktop browser). You should have write your Speaker's notes in your presentation.</li>
	 * <li>Click on <b>View</b> --> <b>Html View</b> (or Ctrl-Alt-Maj-H)
	 * <li><b>Right-Click</b> --> <b>Save as...</b> then save your file.
	 * </ul>
	 * 
	 * @param uri
	 *            : The uri to the Speaker's notes
	 * @return the notes parsed in a SparseArray
	 */
	private SparseArray<String> parseNotesFile(Uri uri) {
		SparseArray<String> notes = new SparseArray<String>();

		String allFile = "";
		InputStream is = null;

		// Save all the inputStream from the Uri in a big String : allFile
		try {
			try {
				is = mActivity.getContentResolver().openInputStream(uri);

				java.util.Scanner s = new java.util.Scanner(is);
				s.useDelimiter("\\A");
				allFile = s.hasNext() ? s.next() : "";
				s.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		int page;
		String split[];
		if (allFile.contains("<html") && allFile.contains("</html>")) {
			// If the file is a Google Presentation Html file
			notes.append(-2, "html");

			if (!allFile.contains("<article")) {
				return notes;
			}
			allFile = allFile.substring(allFile.indexOf("<article"));

			while (allFile.length() > 0 && allFile.indexOf("<article") != -1) {
				allFile = allFile.substring(allFile.indexOf("<article")).split("title=\"Slide ", 2)[1];
				if ((allFile.indexOf("<article") != -1 && allFile.substring(0, allFile.indexOf("<article")).contains("Speaker notes"))
						|| (allFile.indexOf("<article") == -1 && allFile.contains("Speaker notes"))) {
					split = allFile.split("\"", 2);
					page = -1;
					page = Integer.valueOf(split[0]);
					split = split[1].split("<section class=\"slide-notes\" title=\"Speaker notes\">", 2)[1].split("</section>", 2);

					notes.append(page, split[0]);
					Log.d(TAG, "Loaded note " + page + " : " + split[0]);
					allFile = split[1];
				}
			}
		} else {
			// If the file is a plain text file
			notes.append(-2, "plain");
			while (allFile.length() > 0 && allFile.contains("<page=") && allFile.contains(">")) {
				allFile = allFile.substring(allFile.indexOf("<page=") + "<page=".length());
				split = allFile.split(">", 2);
				page = -1;
				page = Integer.valueOf(split[0]);
				notes.append(page, split[1].split("</page>", 2)[0]);
				allFile = allFile.substring("<page=X>".length());
			}
		}

		return notes;
	}

	// Methods called when a display is added or removed. We change the button state if we add or remove a
	// display, and we stop the presentation if there is a display removed.

	@Override
	public void onDisplayAdded(int displayId) {
		checkLaunchable(getView());
	}

	@Override
	public void onDisplayChanged(int displayId) {
	}

	@Override
	public void onDisplayRemoved(int displayId) {
		if (mActivity.getDislay() != null && displayId == mActivity.getDislay().getDisplayId()) {
			mActivity.stopPresentation();
			checkLaunchable(getView());
		}
	}

}
