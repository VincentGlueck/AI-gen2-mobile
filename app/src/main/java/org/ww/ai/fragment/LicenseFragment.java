package org.ww.ai.fragment;

import static org.ww.ai.tools.FileUtil.FILE_UTIL;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ww.ai.databinding.LicenseFragmentBinding;

public class LicenseFragment extends Fragment {

    private Context mContainerContext;

    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        assert container != null;
        this.mContainerContext = container.getContext();
        org.ww.ai.databinding.LicenseFragmentBinding binding = LicenseFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String html = FILE_UTIL.loadStringAsset(mContainerContext,"license.html");
        mWebView = (WebView) view;
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
        mWebView.loadData(html, "text/html; charset=utf-8", "UTF-8");
    }

    public boolean canGoBackMyself() {
        if(mWebView.copyBackForwardList().getCurrentIndex() > 0) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

}
