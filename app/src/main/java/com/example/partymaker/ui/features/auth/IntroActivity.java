package com.example.partymaker.ui.features.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.partymaker.R;
import com.example.partymaker.viewmodel.IntroViewModel;

/**
 * Activity for displaying the intro/welcome slides to new users. Handles ViewPager navigation and
 * onboarding flow.
 */
public class IntroActivity extends AppCompatActivity {

  /** The ViewPager for intro slides. */
  private ViewPager viewPager;

  /** Intro ViewModel */
  private IntroViewModel viewModel;

  /** The layout for the bottom dots indicator. */
  private LinearLayout dotsLayout;

  /** The layouts for each intro slide. */
  private int[] layouts;

  /** Skip and Next buttons. */
  private Button btnSkip, btnNext;

  final ViewPager.OnPageChangeListener viewPagerPageChangeListener =
      new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
          addBottomDots(position);
          viewModel.setCurrentPage(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}

        @Override
        public void onPageScrollStateChanged(int arg0) {}
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set as fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow()
        .setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.activity_intro);

    // Initialize ViewModel
    viewModel = new ViewModelProvider(this).get(IntroViewModel.class);
    setupViewModelObservers();

    viewPager = findViewById(R.id.view_pager);
    dotsLayout = findViewById(R.id.layoutDots);
    btnSkip = findViewById(R.id.btn_skip);
    btnNext = findViewById(R.id.btn_next);

    layouts =
        new int[] {
          R.layout.activity_intro_slider1,
          R.layout.activity_intro_slider2,
          R.layout.activity_intro_slider3
        };

    // adding bottom dots
    addBottomDots(0);

    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
    viewPager.setAdapter(viewPagerAdapter);
    viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
  }

  /** Sets up observers for ViewModel LiveData */
  private void setupViewModelObservers() {
    viewModel
        .getIsLastPage()
        .observe(
            this,
            isLastPage -> {
              if (isLastPage) {
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
              } else {
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
              }
            });

    viewModel
        .getShouldNavigateToLogin()
        .observe(
            this,
            shouldNavigate -> {
              if (shouldNavigate) {
                launchHomeScreen();
              }
            });
  }

  public void btnSkipClick(View v) {
    viewModel.onSkipClicked();
  }

  public void btnNextClick(View v) {
    int current = getItem(1);
    if (current < layouts.length) {
      viewPager.setCurrentItem(current);
    } else {
      viewModel.onNextClicked();
    }
  }

  /**
   * Adds the bottom dots indicator for the intro slides.
   *
   * @param currentPage the current page index
   */
  private void addBottomDots(int currentPage) {
    TextView[] dots = new TextView[layouts.length];

    dotsLayout.removeAllViews();
    for (int i = 0; i < dots.length; i++) {
      dots[i] = new TextView(this);
      dots[i].setText(Html.fromHtml("&#8226;"));
      dots[i].setTextSize(35);
      dots[i].setTextColor(getResources().getColor(R.color.dot_inactive));
      dotsLayout.addView(dots[i]);
    }

    if (dots.length > 0)
      dots[currentPage].setTextColor(getResources().getColor(R.color.dot_active));
  }

  /**
   * Returns the next item index for the ViewPager.
   *
   * @param i the offset
   * @return the next item index
   */
  private int getItem(int i) {
    return viewPager.getCurrentItem() + i;
  }

  /** Launches the LoginActivity and finishes the intro. */
  private void launchHomeScreen() {
    viewModel.resetNavigation();
    startActivity(new Intent(getBaseContext(), LoginActivity.class));
    finish();
  }

  /** PagerAdapter for the intro slides. */
  public class ViewPagerAdapter extends PagerAdapter {

    public ViewPagerAdapter() {}

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
      LayoutInflater layoutInflater =
          (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      View view = layoutInflater.inflate(layouts[position], container, false);
      container.addView(view);

      return view;
    }

    @Override
    public int getCount() {
      return layouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
      return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
      View view = (View) object;
      container.removeView(view);
    }
  }
}
