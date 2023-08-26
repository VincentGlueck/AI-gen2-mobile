package org.ww.ai.fragment;

import static org.ww.ai.event.EventBroker.EVENT_BROKER;
import static org.ww.ai.rds.PagingCache.PAGING_CACHE;
import static org.ww.ai.ui.Animations.ANIMATIONS;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.databinding.GalleryFragmentBinding;
import org.ww.ai.enumif.EventTypes;
import org.ww.ai.enumif.ReceiveEventIF;
import org.ww.ai.prefs.Preferences;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.rds.entity.RenderResultSkeleton;
import org.ww.ai.rds.ifenum.PagingCacheCallbackIF;
import org.ww.ai.rds.ifenum.ThumbnailCallbackIF;
import org.ww.ai.ui.MetricsUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GalleryFragment extends Fragment implements ReceiveEventIF, ThumbnailSelectionCallbackIF, PagingCacheCallbackIF {

    private static final long FADE_TIME = 280L;
    private static final float SCALE_SELECTED = 0.82f;
    private static final float SCALE_FULL = 1.0f;
    private GalleryFragmentBinding mBinding;
    protected LinearLayout mLinearLayout;
    private Context mContainerContext;
    private ViewGroup mViewGroup;
    private MetricsUtil.Screen mScreen;
    protected List<RenderResultSkeleton> mRenderResults;
    private final AtomicBoolean deleteMode = new AtomicBoolean();
    private MenuProvider mMenuProvider;
    protected Set<String> mSelectedSet = new HashSet<>();
    protected boolean mShowTrash = false;

    private AtomicInteger mIdxRender;

    private AtomicInteger mRenderCount;

    private ScrollView mScrollView;

    private AtomicLong mCounter = new AtomicLong(System.currentTimeMillis());

    private ThumbnailCallbackIF mThumbnailCallback;
    private int mInitialLayoutHeight = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedSet.clear();
        writeSelectedToPreferences();
        EVENT_BROKER.registerReceiver(this, EventTypes.SINGLE_IMAGE_DELETED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.mViewGroup = container;
        assert container != null;
        this.mContainerContext = container.getContext();
        mBinding = GalleryFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mLinearLayout = view.findViewById(R.id.results_gallery_linear_layout);
        mScrollView = view.findViewById(R.id.results_gallery_scroll_view);
        view.findViewById(R.id.empty_trash).setVisibility(View.GONE);
        getRenderResultsFromDatabase(mViewGroup);
        if (getActivity() != null && getActivity().getWindowManager() != null) {
            mScreen = MetricsUtil.getScreen(getActivity().getWindowManager());
        }
        PAGING_CACHE.init(mContainerContext);
        mScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            long time = System.currentTimeMillis();
            if ((mCounter.get() + 200L) < time) {
                mThumbnailCallback.onScrollPositionChanged(scrollY, oldScrollY);
            }
        });
        mLinearLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (parent.getHeight() > mScrollView.getMeasuredHeight()
                        && !mThumbnailCallback.isUseDummyImages()) {
                    int rowHeight = ((ViewGroup)parent).getChildAt(0).getHeight();
                    mThumbnailCallback.notifyRowHeight(rowHeight);
                    mThumbnailCallback.setUseDummyImages(true);
                    PAGING_CACHE.setUseDummies(true);
                } else if (mInitialLayoutHeight == -1) {
                    mInitialLayoutHeight = parent.getHeight();
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
    }

    private void getRenderResultsFromDatabase(final ViewGroup viewGroup) {
        AppDatabase appDatabase = AppDatabase.getInstance(mContainerContext);
        ListenableFuture<List<RenderResultSkeleton>> listenableFuture =
                appDatabase.renderResultDao().getAllSkeletons(mShowTrash);
        AsyncDbFuture<List<RenderResultSkeleton>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture,
                r -> createGallery(viewGroup, r), mContainerContext);
    }

    private void createGallery(@NonNull ViewGroup parent,
                               @NonNull List<RenderResultSkeleton> renderResults) {
        mRenderResults = renderResults;
        mRenderCount = new AtomicInteger(0);
        mThumbnailCallback = new ThumbnailCallbackImpl(mContainerContext,
                parent,
                mLinearLayout,
                mSelectedSet,
                this,
                mScreen);
        if (!mRenderResults.isEmpty()) {
            mIdxRender = new AtomicInteger(0);
            PAGING_CACHE.displayThumbnail(mRenderResults.get(0).uid, this, mThumbnailCallback);
        }
    }

    protected void showNothingToDisplayImage() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_result,
                mLinearLayout, false);
        mLinearLayout.addView(emptyView);
        mLinearLayout.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void finishedRender(RenderResultLightWeight lightWeight, LinearLayout singleImageLayout) {
        // TODO: add more magic
        mRenderCount.set(mRenderCount.incrementAndGet());
        if (mRenderCount.get() >= mRenderResults.size()) {
            mThumbnailCallback.processCleanup();
        }
    }

    @Override
    public void initSingleImageView(RenderResultLightWeight lightWeight, LinearLayout layoutHolder) {
        ImageView imageView = layoutHolder.findViewById(R.id.single_gallery_image_view);
        lightWeight.checkBox = layoutHolder.findViewById(R.id.check_single_entry);
        lightWeight.checkBox.setVisibility(View.GONE);
        imageView.setOnClickListener(v -> {
            if (lightWeight.checkBox.isChecked()) {
                lightWeight.checkBox.setChecked(false);
                animateOne(lightWeight, false);
                mSelectedSet = getSelectedSet();
                showCheckOnAll(!mSelectedSet.isEmpty());
            } else {
                onImageClickListener(lightWeight.uid);
            }
        });
        setOnLongClickListener(lightWeight, imageView);
        lightWeight.checkBox.setOnCheckedChangeListener((v, isChecked) -> {
            if (!isChecked) {
                animateOne(lightWeight, false);
            } else {
                animateOne(lightWeight, true);
            }
            mSelectedSet = getSelectedSet();
            showCheckOnAll(!mSelectedSet.isEmpty());
            updateToolbar();
        });
    }

    private void setOnLongClickListener(RenderResultLightWeight lightWeight, ImageView imageView) {
        imageView.setOnLongClickListener(l -> {
            lightWeight.checkBox.setChecked(!lightWeight.checkBox.isChecked());
            animateOne(lightWeight, lightWeight.checkBox.isChecked());
            updateToolbar();
            mSelectedSet = getSelectedSet();
            if (mSelectedSet != null) {
                showCheckOnAll(!mSelectedSet.isEmpty());
            }
            return true;
        });
    }

    private void animateOne(RenderResultLightWeight lightWeight, boolean decreaseSize, int... time) {
        float from = SCALE_SELECTED;
        float to = SCALE_FULL;
        if (decreaseSize) {
            float f = from;
            from = to;
            to = f;
        }
        long delay = FADE_TIME;
        if (time.length > 0) {
            delay = time[0];
        }
        final Animation animation = ANIMATIONS.getScaleAnimation(from, to, delay, true);
        View view = (View) lightWeight.checkBox.getParent();
        if (view != null) {
            view.startAnimation(animation);
        }
    }

    private void showCheckOnAll(boolean visible) {
        if (mRenderResults == null) {
            return;
        }
        // mRenderResults.forEach(r -> r.checkBox.setVisibility(visible ? View.VISIBLE : View.GONE));
    }

    private void removeFromView(ViewGroup root, RenderResultLightWeight lightweight) {
        if (root == null) {
            return;
        }
        mRenderResults.stream().filter(l -> lightweight.uid == l.uid).forEach(r -> {
            /*
            ViewParent parent = r.checkBox.getParent();
            ViewGroup rowParent = (ViewGroup) parent.getParent();
            if (rowParent != null) {
                rowParent.removeView((View) parent);
                if (rowParent.getChildCount() == 0) {
                    root.removeView(rowParent);
                }
            }

             */
        });
    }

    protected void updateToolbar() {
        if (mRenderResults != null) {
            /*
            boolean deleteChecked = mRenderResults.stream().anyMatch(r -> r.checkBox.isChecked());
            if (!deleteMode.get() && deleteChecked) {
                addMenuToolbar();
                deleteMode.set(true);
            }
            if (!deleteChecked && deleteMode.get()) {
                removeMenuToolbar();
            }

             */
        }
    }

    protected void removeMenuToolbar() {
        requireActivity().removeMenuProvider(mMenuProvider);
        mMenuProvider = null;
        deleteMode.set(false);
    }

    protected int getMenuResourceId() {
        return R.menu.gallerymenu;
    }

    protected void handleMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_delete) {
            mSelectedSet = getSelectedSet();
            if (mSelectedSet != null && !mSelectedSet.isEmpty()) {
                performDelete();
            }
        }
    }

    private void addMenuToolbar() {
        if (mMenuProvider != null) {
            requireActivity().removeMenuProvider(mMenuProvider);
        }
        MenuHost menuHost = requireActivity();
        mMenuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(getMenuResourceId(), menu);

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                handleMenuItemSelected(menuItem);
                return false;
            }
        };
        menuHost.addMenuProvider(mMenuProvider);
    }


    private void performDelete() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        final boolean useTrash = Preferences.getInstance(requireContext()).getBoolean(Preferences.PREF_USE_TRASH);
        mSelectedSet.forEach(r -> {
            ListenableFuture<RenderResult> future = db.renderResultDao().getById(Integer.parseInt(r));
            if (useTrash) {
                softDeleteFuture(db, future, true);
            } else {
                hardDeleteFuture(db, future);
            }
        });
        // removeDeletedViewsFromParent();
        if (mRenderResults.isEmpty()) {
            showNothingToDisplayImage();
        }
    }

    /*
    protected void removeDeletedViewsFromParent() {
        mSelectedSet.forEach(uid -> {
            Optional<RenderResultSkeleton> optional = mRenderResults.stream()
                    .filter(r -> r.uid == Integer.parseInt(uid)).findFirst();
            optional.ifPresent(skeleton -> {
                ListenableFuture<List<RenderResultLightWeight>> future = PAGING_CACHE.getById(skeleton.uid);
                AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
                asyncDbFuture.processFuture(future, result -> {
                    Optional<RenderResultLightWeight> optional1 = result.stream().parallel().filter(f -> f.uid == optional.get().uid).findFirst();
                    if (optional1.isPresent()) {
                        removeFromView(mLinearLayout, optional1.get());
                        optional1.get().checkBox.setChecked(false);
                    }
                }, mContainerContext);
                mRenderResults.remove(skeleton);
            });
        });
        mSelectedSet.clear();
    }

     */

    protected void softDeleteFuture(AppDatabase db, ListenableFuture<RenderResult> future,
                                    boolean setDeleteFlagTo) {
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            result.deleted = setDeleteFlagTo;
            ListenableFuture<Integer> softDelFuture = db.renderResultDao().updateRenderResults(List.of(result));
            AsyncDbFuture<Integer> asyncDbFuture1 = new AsyncDbFuture<>();
            asyncDbFuture1.processFuture(softDelFuture, i -> {
            }, requireContext());
        }, requireContext());

    }

    private void hardDeleteFuture(AppDatabase db, ListenableFuture<RenderResult> future) {
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            ListenableFuture<Integer> delFuture = db.renderResultDao().deleteRenderResults(List.of(result));
            AsyncDbFuture<Integer> asyncDbFutureDel = new AsyncDbFuture<>();
            asyncDbFutureDel.processFuture(delFuture, i -> {
            }, requireContext());
        }, requireContext());
    }


    @Override
    public void onPause() {
        super.onPause();
        mSelectedSet = getSelectedSet();
        writeSelectedToPreferences();
    }

    private void writeSelectedToPreferences() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(
                GalleryFragment.class.getCanonicalName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("sel", mSelectedSet);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = requireActivity().getSharedPreferences(
                GalleryFragment.class.getCanonicalName(), Context.MODE_PRIVATE);
        mSelectedSet = new HashSet<>();
        mSelectedSet = preferences.getStringSet("sel", mSelectedSet);
        // markSelected(mSelectedSet);
        if (!mSelectedSet.isEmpty()) {
            addMenuToolbar();
        }
    }

    /*
    private void markSelected(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return;
        }
        if (mRenderResults == null) {
            return;
        }
        for (RenderResultSkeleton renderResult : mRenderResults) {
            for (String str : set) {
                if (String.valueOf(renderResult.uid).equals(str)) {
                    renderResult.checkBox.setChecked(true);
                    ListenableFuture<List<RenderResultLightWeight>> future = PAGING_CACHE.getById(renderResult.uid);
                    AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
                    asyncDbFuture.processFuture(future, result -> {
                        RenderResultLightWeight lw = PAGING_CACHE.getFromResult(renderResult.uid, result);
                        animateOne(lw, renderResult.checkBox.isChecked(), 1);
                    }, mContainerContext);

                }
            }
        }
        mSelectedSet = getSelectedSet();
        showCheckOnAll(mSelectedSet != null && !mSelectedSet.isEmpty());
    }

     */

    protected Set<String> getSelectedSet() {
        return Collections.emptySet();
        /*
        return mRenderResults.stream().filter(r -> r.checkBox.isChecked())
                .map(m -> String.valueOf(m.uid)).collect(Collectors.toSet());

         */
    }

    private void onImageClickListener(int uid) {
        NavController navController = NavHostFragment.findNavController(GalleryFragment.this);
        Bundle bundle = new Bundle();
        bundle.putInt(RenderDetailsFragment.ARG_UID, uid);
        navController.navigate(R.id.action_GalleryFragment_to_GalleryFullSizeFragment, bundle);
    }

    private LinearLayout createRow(ViewGroup parent) {
        return (LinearLayout) LayoutInflater.from(requireContext())
                .inflate(R.layout.single_gallery_row, parent, false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMenuProvider != null) {
            requireActivity().removeMenuProvider(mMenuProvider);
        }
        mBinding = null;
    }

    @Override
    public void onDetach() {
        mSelectedSet = new HashSet<>();
        writeSelectedToPreferences();
        super.onDetach();
    }

    @Override
    public void receiveEvent(Object... eventObject) {
        if (eventObject.length > 0) {
            int removeThumbUid = Integer.parseInt(eventObject[0].toString());
            getRenderResultFromCollection(removeThumbUid);
        }
    }

    private void getRenderResultFromCollection(int uid) {
        AppDatabase appDatabase = AppDatabase.getInstance(mContainerContext);
        ListenableFuture<RenderResult> future = appDatabase.renderResultDao().getById(uid);
        AsyncDbFuture<RenderResult> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            mSelectedSet.clear();
            mSelectedSet.add(String.valueOf(result.uid));
            // removeDeletedViewsFromParent();
        }, mContainerContext);
    }

    @Override
    public void cachingDone() {
        mIdxRender.set(mIdxRender.incrementAndGet());
        if (mIdxRender.get() < mRenderResults.size()) { //  && !mScreenSizeReached.get()
            PAGING_CACHE.displayThumbnail(mRenderResults.get(mIdxRender.get()).uid, this, mThumbnailCallback);
        }
    }
}
