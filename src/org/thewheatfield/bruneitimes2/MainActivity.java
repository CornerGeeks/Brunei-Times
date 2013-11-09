package org.thewheatfield.bruneitimes2;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements IDownloadPageCallback {
	private static final String STATE_POSITION = "STATE_POSITION";
	private static final String STATE_DATE = "STATE_DATE";
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ViewPager pager;
	private String mDate;
	private BTData mDatabase;
	private int mPages;
	private final int DOWNLOADED_WIDTH = 1200;
	private final int DOWNLOADED_HEIGHT = 1613;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getApplicationContext().deleteDatabase("BruneiTimesData.db");
		mDatabase = new BTData(this);
		setContentView(R.layout.ac_image_pager);

		File cacheDir = mDatabase.getCacheDir();

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc(true).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCache(
						new UnlimitedDiscCache(cacheDir, new BTFileGenerator()))
				.memoryCacheExtraOptions(DOWNLOADED_WIDTH, DOWNLOADED_HEIGHT)
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		int pagerPosition = 0;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			pagerPosition = bundle.getInt(BT.IMAGE_POSITION, 0);
			mDate = bundle.getString("date");
		} else {
			mDate = BT.DATE_FORMAT.format(new Date());
		}
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
			mDate = savedInstanceState.getString(STATE_DATE);
		}
		loadPaper(pagerPosition);
	}

	private void loadPaper(int position) {
		List<String> images = new ArrayList<String>();
		mPages = mDatabase.getPages(mDate);
		if (mPages > 0) {
			setAppTitleDate(mDate);
			images = loadImageArray(mDate, mPages);
		} else {
			downloadNumPagesAsShow(mDate);
			images.add("drawable://" + R.drawable.ic_launcher); // Image from
																// drawables");
		}
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(images.toArray(new String[images
				.size()])));
		pager.setCurrentItem(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
		outState.putString(STATE_DATE, mDate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			reload(mDate);
			return true;
		case R.id.action_share:
			shareCurrentPage();
			return true;
		case R.id.action_open_with:
			openInGallery();
			return true;
			// case R.id.action_clear_cache:
			// imageLoader.clearMemoryCache();
			// imageLoader.clearDiscCache();
			// return true;
		case R.id.action_today:
			mDate = BT.DATE_FORMAT.format(new Date());
			loadPaper(0);
			return true;
		case R.id.action_select_page:
			showPageSelection();
			return true;
		case R.id.action_select_date:
			showDateSelection();
			return true;
		default:
			return false;
		}
	}

	public void reload(String date) {
		mDatabase.deletePages(date);
		mDatabase.deleteCache(date);
		loadPaper(0);
	}

	public void shareCurrentPage() {
		File imageToShare = new File(mDatabase.getCacheDir(),
				BTFileGenerator.getFileName(mDate, pager.getCurrentItem() + 1));
		if (imageToShare.exists()) {
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + imageToShare.getAbsolutePath()));
			shareIntent.setType("image/jpeg");
			startActivity(Intent.createChooser(shareIntent, getResources()
					.getText(R.string.action_share)));
		}
	}

	public void openInGallery() {
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_VIEW);
		File imageToShare = new File(mDatabase.getCacheDir(),
				BTFileGenerator.getFileName(mDate, pager.getCurrentItem() + 1));
		if (imageToShare.exists()) {
			shareIntent.setDataAndType(Uri.fromFile(imageToShare), "image/*");
			startActivity(shareIntent);
		}
	}

	public void showPageSelection() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		ArrayList<String> pages = new ArrayList<String>();
		int selected = pager.getCurrentItem();
		for (int i = 1; i <= mPages; i++)
			pages.add("" + i);
		// builder.setTitle(R.string.action_select_page)
		// .setSingleChoiceItems(pages.toArray(new String[mPages]), selected,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// Log.d("BTMain", "Page selected: " + which);
		// pager.setCurrentItem(which);
		// }
		// });

		builder.setTitle(R.string.action_select_page).setSingleChoiceItems(
				pages.toArray(new String[mPages]), selected,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d("BTMain", "Page selected: " + which);
						pager.setCurrentItem(which);
						dialog.dismiss();
					}
				});

		builder.show();
	}

	public void showDateSelection() {
		final DatePicker dp = new DatePicker(this);
		GregorianCalendar gcMax = new GregorianCalendar();
		gcMax.add(GregorianCalendar.DATE, 1);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			dp.setMaxDate(gcMax.getTime().getTime());
			dp.setCalendarViewShown(false);
			dp.setSpinnersShown(true);
		}
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(BT.DATE_FORMAT.parse(mDate));
			dp.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DATE), null);
		} catch (Exception e) {

		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.action_select_date)
				.setView(dp)
				.setPositiveButton(R.string.use_this_date,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mDate = (dp.getYear() % 100) + ""
										+ (dp.getMonth() + 1) + ""
										+ (dp.getDayOfMonth() < 10 ? "0" : "")
										+ "" + dp.getDayOfMonth();
								Log.d("BTMain",
										"Selected date: " + dp.getYear() + "-"
												+ (dp.getMonth() + 1) + "-"
												+ dp.getDayOfMonth());
								loadPaper(0);
							}
						});
		builder.show();
	}

	private void downloadNumPagesAsShow(String date) {
		setAppTitle(getResources().getString(R.string.loading) + " "
				+ BT.getDisplayDate(date));
		DownloadNumberOfPages task = new DownloadNumberOfPages(this);
		Log.d("BTMain", "download page number date: " + date);
		task.execute(date);
	}

	@Override
	public void process(String data) {
		try {
			setAppTitleDate(mDate);
			Log.d("BTMain", "pages response: " + data);
			mPages = Integer.parseInt(data);
			Log.d("BTMain",
					"saving to db: " + mDatabase.savePages(mDate, mPages));

			if (mPages > 0) {
				loadPaper(0);
			} else {
				Toast.makeText(this, R.string.no_pages_found, Toast.LENGTH_LONG)
						.show();
			}
			// pager.setCurrentItem();
		} catch (Exception e) {
			Toast.makeText(this, R.string.unexpected_error, Toast.LENGTH_LONG)
					.show();
			Log.d("BTMain", e.getMessage());
			Log.d("BTMain", e.getStackTrace().toString());
		}
	}

	private List<String> loadImageArray(String date, int pages) {
		List<String> images = new ArrayList<String>();
		if (date != null && pages > 0) {
			// "http://epaper.bt.com.bn/bruneitimes/ipad/getZoom.jsp?id=131014bruneitimes&file=Zoom-1.jpg",
			// 1200 x 1613
			for (int i = 0; i < pages; i++) {
				images.add("http://epaper.bt.com.bn/bruneitimes/ipad/getZoom.jsp?id="
						+ date + "bruneitimes&file=Zoom-" + (i + 1) + ".jpg");
			}
		}
		return images;
	}

	private void setAppTitleDate(String date) {
		setAppTitle(BT.getDisplayDate(date));
	}

	private void setAppTitle(String string) {
		if (android.os.Build.VERSION.SDK_INT >= 11)
			getActionBar().setTitle(string);
		else
			setTitle(string);
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private String[] images;
		private LayoutInflater inflater;

		ImagePagerAdapter(String[] images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image,
					view, false);

			PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image);
			imageView.setMinimumScale(1.0f);
			imageView.setMediumScale(4.0f);
			imageView.setMaximumScale(4.0f);

			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);

			imageLoader.displayImage(images[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							Toast.makeText(MainActivity.this, message,
									Toast.LENGTH_SHORT).show();

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
						}
					});

			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}

}
