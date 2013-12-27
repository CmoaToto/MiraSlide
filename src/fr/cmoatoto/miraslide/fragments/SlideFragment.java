package fr.cmoatoto.miraslide.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import fr.cmoatoto.miraslide.MainActivity;
import fr.cmoatoto.miraslide.PdfViewerPresentation;
import fr.cmoatoto.miraslide.R;

/**
 * This fragment is only an ImageView, the same as the {@link PdfViewerPresentation}, but with a white background. <br>
 * It gets the number of the page as argument, then get the appropriate bitmap from the pdf.
 * 
 * @author CmoaToto
 */
public class SlideFragment extends Fragment {

	public static final String KEY_PAGE = "fr.cmoatoto.miraslide.fragments.SlideFragment.KeyPage";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_slide, null);

		final boolean html = ((MainActivity) getActivity()).getNotes().get(-2).equals("html");

		// On Click on the SwitchView button, we switch between the slide view and the notes view.
		v.findViewById(R.id.fragment_slide_button_switchview).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getView().findViewById(R.id.fragment_slide_imageview_slide).getVisibility() == View.GONE) {
					((ImageButton) v).setImageResource(R.drawable.ic_action_text);
					getView().findViewById(R.id.fragment_slide_imageview_slide).setVisibility(View.VISIBLE);
					getView().findViewById(R.id.fragment_slide_scrollview_note).setVisibility(View.GONE);
					getView().findViewById(R.id.fragment_slide_button_zoomin).setVisibility(View.GONE);
					getView().findViewById(R.id.fragment_slide_button_zoomout).setVisibility(View.GONE);
				} else {
					((ImageButton) v).setImageResource(R.drawable.ic_action_picture);
					getView().findViewById(R.id.fragment_slide_imageview_slide).setVisibility(View.GONE);
					getView().findViewById(R.id.fragment_slide_scrollview_note).setVisibility(View.VISIBLE);
					getView().findViewById(R.id.fragment_slide_button_zoomin).setVisibility(View.VISIBLE);
					getView().findViewById(R.id.fragment_slide_button_zoomout).setVisibility(View.VISIBLE);
				}
			}
		});

		// On click on the zoom in and zoom out button, we (un)zoom the webview or change the textview text size
		v.findViewById(R.id.fragment_slide_button_zoomin).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (html) {
					((WebView) getView().findViewById(R.id.fragment_slide_textview_note_html)).zoomIn();
				} else {
					float size = ((TextView) getView().findViewById(R.id.fragment_slide_textview_note_txt)).getTextSize();
					((TextView) getView().findViewById(R.id.fragment_slide_textview_note_txt)).setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 1.2f);
				}
			}
		});
		v.findViewById(R.id.fragment_slide_button_zoomout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (html) {
					((WebView) getView().findViewById(R.id.fragment_slide_textview_note_html)).zoomOut();
				} else {
					float size = ((TextView) getView().findViewById(R.id.fragment_slide_textview_note_txt)).getTextSize();
					((TextView) getView().findViewById(R.id.fragment_slide_textview_note_txt)).setTextSize(TypedValue.COMPLEX_UNIT_PX, size / 1.2f);
				}
			}
		});

		int page = getArguments().getInt(KEY_PAGE);
		((ImageView) v.findViewById(R.id.fragment_slide_imageview_slide)).setImageBitmap(((MainActivity) getActivity()).getPresentation().getPage(page));

		String note = ((MainActivity) getActivity()).getNotes().get(page + 1);

		// If there is a note for the slide, we show the note first (the user can switch to the slide's image with the display button). If there is no note, we
		// show the image and hide the zoom buttons
		if (note != null && note.length() > 0) {
			((ImageButton) v.findViewById(R.id.fragment_slide_button_switchview)).setImageResource(R.drawable.ic_action_picture);
			if (html) {
				((WebView) v.findViewById(R.id.fragment_slide_textview_note_html)).loadData(note, "text/html;charset=UTF-8", null);
				v.findViewById(R.id.fragment_slide_textview_note_txt).setVisibility(View.GONE);
			} else {
				((TextView) v.findViewById(R.id.fragment_slide_textview_note_txt)).setText(note);
				v.findViewById(R.id.fragment_slide_textview_note_html).setVisibility(View.GONE);
			}
			v.findViewById(R.id.fragment_slide_imageview_slide).setVisibility(View.GONE);
			v.findViewById(R.id.fragment_slide_scrollview_note).setVisibility(View.VISIBLE);
			v.findViewById(R.id.fragment_slide_button_zoomin).setVisibility(View.VISIBLE);
			v.findViewById(R.id.fragment_slide_button_zoomout).setVisibility(View.VISIBLE);
			v.findViewById(R.id.fragment_slide_button_switchview).setEnabled(true);
		} else {
			v.findViewById(R.id.fragment_slide_imageview_slide).setVisibility(View.VISIBLE);
			v.findViewById(R.id.fragment_slide_scrollview_note).setVisibility(View.GONE);
			v.findViewById(R.id.fragment_slide_button_zoomin).setVisibility(View.GONE);
			v.findViewById(R.id.fragment_slide_button_zoomout).setVisibility(View.GONE);
			v.findViewById(R.id.fragment_slide_button_switchview).setVisibility(View.GONE);
		}

		return v;
	}
}
