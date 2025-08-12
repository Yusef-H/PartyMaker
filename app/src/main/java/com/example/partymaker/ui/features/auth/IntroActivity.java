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
import com.example.partymaker.viewmodel.auth.IntroViewModel;

/**
 * Activity for displaying the intro/welcome slides to new users. Handles ViewPager navigation and
 * onboarding flow.
 */
public class IntroActivity extends AppCompatActivity {

  // UI constants
  private static final int DOT_TEXT_SIZE = 35;
  private static final int PAGE_OFFSET = 1;
  private static final String DOT_HTML_ENTITY = "&#8226;";

  // Slide layout resources
  private static final int[] SLIDE_LAYOUTS = {
    R.layout.activity_intro_slider1,
    R.layout.activity_intro_slider2,
    R.layout.activity_intro_slider3
  };

  /** The ViewPager for intro slides. */
  private ViewPager viewPager;

  /** Intro ViewModel */
  private IntroViewModel viewModel;

  /** The layout for the bottom dots indicator. */
  private LinearLayout dotsLayout;

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

    // Layouts are now defined as a constant

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
    int current = getItem();
    if (current < SLIDE_LAYOUTS.length) {
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
    TextView[] dots = createDots();
    addDotsToLayout(dots);
    setActiveDot(dots, currentPage);
  }

  private TextView[] createDots() {
    TextView[] dots = new TextView[SLIDE_LAYOUTS.length];
    for (int i = 0; i < dots.length; i++) {
      dots[i] = createDotTextView();
    }
    return dots;
  }

  private TextView createDotTextView() {
    TextView dot = new TextView(this);
    dot.setText(Html.fromHtml(DOT_HTML_ENTITY));
    dot.setTextSize(DOT_TEXT_SIZE);
    dot.setTextColor(getResources().getColor(R.color.dot_inactive));
    return dot;
  }

  private void addDotsToLayout(TextView[] dots) {
    dotsLayout.removeAllViews();
    for (TextView dot : dots) {
      dotsLayout.addView(dot);
    }
  }

  private void setActiveDot(TextView[] dots, int currentPage) {
    if (dots.length > 0 && currentPage >= 0 && currentPage < dots.length) {
      dots[currentPage].setTextColor(getResources().getColor(R.color.dot_active));
    }
  }

  /**
   * Returns the next item index for the ViewPager.
   *
   * @return the next item index
   */
  private int getItem() {
    return viewPager.getCurrentItem() + IntroActivity.PAGE_OFFSET;
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

      View view = layoutInflater.inflate(SLIDE_LAYOUTS[position], container, false);
      container.addView(view);

      return view;
    }

    @Override
    public int getCount() {
      return SLIDE_LAYOUTS.length;
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
