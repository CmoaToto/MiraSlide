package fr.cmoatoto.miraslide.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import fr.cmoatoto.miraslide.MainActivity;
import fr.cmoatoto.miraslide.PdfViewerPresentation;
import fr.cmoatoto.miraslide.R;

/**
 * This fragment is only an ImageView, the same as the {@link PdfViewerPresentation}, but with a white background.
 * <br>
 * It gets the number of the page as argument, then get the appropriate bitmap from the pdf.
 * 
 * @author CmoaToto
 */
public class SlideFragment extends Fragment {

	public static final String KEY_PAGE = "fr.cmoatoto.miraslide.fragments.SlideFragment.KeyPage";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = getActivity().getLayoutInflater().inflate(R.layout.presentation_main, null);

		v.setBackgroundColor(Color.WHITE);

		int page = getArguments().getInt(KEY_PAGE);
		((ImageView) v).setImageBitmap(((MainActivity) getActivity()).getPresentation().getPage(page));

		return v;
	}

}
