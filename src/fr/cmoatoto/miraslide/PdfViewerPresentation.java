package fr.cmoatoto.miraslide;

import java.io.File;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.Toast;
import cx.hell.android.lib.pagesview.RenderingException;
import cx.hell.android.lib.pagesview.Tile;
import cx.hell.android.lib.pdf.PDF;
import cx.hell.android.pdfviewpro.PDFPagesProvider;

/**
 * This is the Presentation class, extended to show PDF slides.
 * 
 * @author CmoaToto
 */
public class PdfViewerPresentation extends Presentation {

	private final static String TAG = PdfViewerPresentation.class.getName();

	/** The PDF object describing the pdf file */
	private PDF mPdf = null;

	/** The pdf page provider let us get things from the pdf file. Here we need bitmap for each page */
	private PDFPagesProvider mPdfPagesProvider = null;

	/** Element required for native classes */
	private int box = 2;

	/** Path to pdf file */
	private String mPdfFilePath = null;

	/** this view is the only view of the presentation, where we show the bitmaps */
	private ImageView mImageView;

	/** this Point stores the size of the Display to get the best Bitmaps. */
	private Point mOutSize;

	/** Current page */
	private int mPage = 0;

	/** Sizes of all the pages of the pdf */
	private int[][] pagesSize;

	/** Constructor. It creates the Presentation, then load the Pdf to show */
	public PdfViewerPresentation(Context context, Display display, String pdfFilePath) {
		super(context, display);
		mPdfFilePath = pdfFilePath;
		init();
	}

	/** Constructor. It creates the Presentation, then load the Pdf to show */
	public PdfViewerPresentation(Context context, Display display, int theme, String pdfFilePath) {
		super(context, display, theme);
		mPdfFilePath = pdfFilePath;
		init();
	}

	/** Get the size of the display then load the Pdf to show */
	private void init() {
		mOutSize = new Point();
		getDisplay().getSize(mOutSize);
		startPDF();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate(" + savedInstanceState + ")");

		createContentView();

		showPage();
	}

	/** Show the current page on the Presentation view */
	private void showPage() {
		mImageView.setImageBitmap(getPage(mPage));
	}

	/** return the Bitmap of the pdf specified page */
	public Bitmap getPage(int page) {
		Tile tile = new Tile(page, 1000, 0, 0, 0, pagesSize[page][0], pagesSize[page][1]);
		Bitmap bm = null;
		try {
			bm = mPdfPagesProvider.renderBitmap(tile);
			if (bm == null) {
				bm = mPdfPagesProvider.getPageBitmap(tile);
			}
		} catch (RenderingException e) {
			e.printStackTrace();
		}
		return bm;
	}

	/** Create the imageView to show the pdf pages Bitmaps */
	private void createContentView() {
		mImageView = (ImageView) getLayoutInflater().inflate(R.layout.presentation_main, null);
		setContentView(mImageView);
	}

	/** create the PDF object, PDF Page Provider etc..., based on the pdfFilePath */
	private void startPDF() {
		mPdf = getPDF();
		if (!mPdf.isValid()) {
			Log.v(TAG, "Invalid PDF");
			if (mPdf.isInvalidPassword()) {
				Toast.makeText(getContext(), "This file needs a password", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getContext(), "Invalid PDF file", Toast.LENGTH_SHORT).show();
			}
			return;
		}
		mPdfPagesProvider = new PDFPagesProvider(mOutSize, mPdf, false, true);
		pagesSize = mPdfPagesProvider.getPageSizes();
	}

	/** return PDF instance wrapping file referenced by filepath. It reads all bytes to memory. */
	private PDF getPDF() {
		return new PDF(new File(mPdfFilePath), box);
	}

	/** Move the presentation to 'moveby' pages. It can be 1, -1 or anything... */
	public void moveBy(int moveBy) {
		if (mPage + moveBy >= 0 && mPage + moveBy < getPageCount()) {
			mPage += moveBy;
			showPage();
		}
	}

	/** Move the presentation to the page 'page' */
	public void moveTo(int page) {
		if (page >= 0 && page < getPageCount()) {
			mPage = page;
			showPage();
		}
	}

	/** return the number of page of the presentation */
	public int getPageCount() {
		return mPdf.getPageCount();
	}

}
